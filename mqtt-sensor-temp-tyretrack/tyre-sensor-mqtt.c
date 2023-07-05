#include "contiki.h"

#include "net/routing/routing.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"

#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "sys/process.h"

#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"

#include "tyre-sensor-mqtt.h"

#include "os/sys/log.h"

#include "mqtt.h"
#include "mqtt-prop.h"

#include <string.h>
#include <strings.h>
#include <stdarg.h>
#include <time.h>
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "Car-Sensor"
#define LOG_LEVEL LOG_LEVEL_DBG
/*------------------------------------*/
/*             INIT PHASE             */
/*------------------------------------*/

// Broker address
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"
#define DEFAULT_BROKER_PORT 1883
static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Config values
#define ID_PAIR 1

// Sizes and Lenghts
#define MAX_TCP_SEGMENT_SIZE 32
#define CONFIG_IP_ADDR_STR_LEN 64
#define BUFFER_SIZE 64
#define PUB_BUFFER_SIZE 128


// Buffer for topic publication
#define SUB_TOPIC "SetThreshold"
#define PUB_TOPIC "TyreTemp"

// status mqtt
mqtt_status_t status;

static struct mqtt_connection conn;
char broker_addr[CONFIG_IP_ADDR_STR_LEN];

// mqtt_message data structure
static struct mqtt_message *msg_ptr = 0;
/* msg_pointer->topic - Contains the topic
 * msg_pointer->payload_chunk - Contains the payload
 * msg_pointer->payload_lenght - Contains the payload lenghts
 */

// Buffers
static char client_id[BUFFER_SIZE];
static char pub_buffer[PUB_BUFFER_SIZE];

#define ECHO_REQ_PAYLOAD_LEN   20
/*------------------------------------*/
/*                STATE               */
/*------------------------------------*/

// Timer
#define CONNECTION_FREQUENCE (CLOCK_SECOND * 5)
#define SAMPLING_FREQUENCE (CLOCK_SECOND * 2)

int state_machine_timer = SAMPLING_FREQUENCE;
static struct etimer periodic_state_timer;

// States
static uint8_t state;

#define STATE_INIT              0
#define STATE_NET_OK            1
#define STATE_CONNECTING        2
#define STATE_CONNECTED         3
#define STATE_SUBSCRIBED        4
#define STATE_DISCONNECTED      5
#define STATE_ERROR             6

// Process
PROCESS(mqtt_client_process, "MQTT Track");
AUTOSTART_PROCESSES(&mqtt_client_process);

/*------------------------------------*/
/*             TIMESTAMP              */
/*------------------------------------*/

static char timeStr[20];
static void setTimeStamp(void)
{
       time_t timestamp;
       time(&timestamp);

      // Convert timestamp to a formatted string
       struct tm* timeinfo;
       timeinfo = localtime(&timestamp);
      strftime(timeStr, sizeof(timeStr), "%Y-%m-%d %H:%M:%S", timeinfo);
}

/*------------------------------------*/
/*        GESTIONE TEMPERATURA        */
/*------------------------------------*/

static int temperature = 700;
enum trend
{
    PUSH,
    NORMAL,
    SLOW
};

static enum trend driver_mode = NORMAL;

static void simulate_temperature ()
{
    // LOG_DBG("driver_mode = %d\n", driver_mode);

    if (temperature > 900 && driver_mode == NORMAL)
    {
        LOG_INFO("Chage mood: PUSH\n");
        driver_mode = PUSH;
    }

    if (temperature > 1050 && (driver_mode == PUSH || driver_mode == NORMAL))
    {
        LOG_INFO("Chage mood: SLOW\n");
        driver_mode = SLOW;
    }

    if (temperature < 850 && driver_mode == SLOW)
    {
        LOG_INFO("Chage mood: PUSH\n");
        driver_mode = PUSH;
    }
    
    if (driver_mode == PUSH)
    {
        temperature += 5;
    }
    else if (driver_mode == NORMAL)
    {
        temperature += 1;
    }
    else if (driver_mode == SLOW)
    {
        temperature -= 5;
    }

    // LOG_DBG("Temperature = %d\n", temperature);
}

/*-------------------------------------------------*/
/*         GESTIONE DEI MESSAGGI IN ARRIVO         */
/*-------------------------------------------------*/
static void handler_incoming_msg(const char *topic, const uint8_t *chunk) 
{
	LOG_INFO("Message received at topic '%s': %s\n", topic, chunk);

    // Cambiare l'intervallo di cambionamento
    int timer_value = (CLOCK_SECOND * (int)*msg_ptr->payload_chunk);
    state_machine_timer = timer_value;
    etimer_set(&periodic_state_timer, state_machine_timer);
}
/*------------------------------------*/
/*         CHECK CONNECTIVITY         */
/*------------------------------------*/
static bool have_conn(void)
{
    //Ritorna true solo se il nodo corrente ha un Public IP
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL)
    {
        return false;
    }
    return true;
}

static void ping_parent(void)
{
    if(have_conn())
    {
        uip_icmp6_send(uip_ds6_defrt_choose(), ICMP6_ECHO_REQUEST, 0, ECHO_REQ_PAYLOAD_LEN);
    }
}

/*------------------------------------*/
/*    MQTT_EVENT CALLBACK FUNCTION    */
/*------------------------------------*/
/* Quando viene istanziata una connessione all'MQTT broker
    va specificata questa callback function, la quale viene
    infocata ogni volta che arriva un evento MQTT
    a seconda di quale evento arriva, va gestito in modo diverso */
static void mqtt_event (struct mqtt_connection *m, mqtt_event_t event, void *data)
{
    switch (event)
    {
        case MQTT_EVENT_CONNECTED:
            /* Connessione riuscita */
            LOG_INFO("MQTT connection event\n");
            state = STATE_CONNECTED;
            /*-------------------------*/
            break;

        case MQTT_EVENT_DISCONNECTED:
            LOG_INFO("MQTT disconnected\n");
            /* Disconnessione */
            state = STATE_DISCONNECTED;
            /*-------------------------*/
            break;

        case MQTT_EVENT_PUBLISH:
            LOG_INFO("MQTT PUBLISH EVENT\n");
            /* Qualcuno ha publicato dove sono subscribed */
            msg_ptr = data;
            handler_incoming_msg(msg_ptr->topic, msg_ptr->payload_chunk);
            /*-------------------------*/
            break;

        case MQTT_EVENT_SUBACK:
            /* Subscribe riuscito */
            #if MQTT_311
                mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

                if(suback_event->success) 
                {
                    LOG_INFO("Application is subscribed to topic successfully\n");
                } 
                else 
                {
                    LOG_INFO("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
                }
            #else
                LOG_INFO("Application is subscribed to topic successfully\n");
            #endif
            /*-------------------------*/
            break;

        case MQTT_EVENT_UNSUBACK:
            /* Unsubscribe riuscito */
            LOG_INFO("Application is unsubscribed to topic successfully\n");
            /*-------------------------*/
            break;

        case MQTT_EVENT_PUBACK:
            /* Publicazione riuscita */
            LOG_INFO("Publishing complete.\n");
            /*-------------------------*/
            break;

        case MQTT_EVENT_CONNECTION_REFUSED_ERROR:
            /* Publicazione riuscita */
            state = STATE_ERROR;
            /*-------------------------*/
            break;

        default:
            LOG_INFO("Application got a unhandled MQTT event: %i\n", event);
            break;
    }
}
/*------------------------------------*/
/*         INITIALIZE CLIENTID        */
/*------------------------------------*/
static void client_init(void)
{
    etimer_set(&periodic_state_timer, CONNECTION_FREQUENCE);
    int len = snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
            linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
            linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
            linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

    /* len < 0: Error. Len >= BUFFER_SIZE: Buffer too small */
    if(len < 0 || len >= BUFFER_SIZE) 
    {
        LOG_ERR("Client ID: %d, Buffer %d\n", len, BUFFER_SIZE);
    }

    state = STATE_INIT;
}

/*------------------------------------*/
/*            MQTT_REGISTER           */
/*------------------------------------*/
static void connect_mqtt()
{
    memcpy(broker_addr, broker_ip, strlen(broker_ip));

    mqtt_connect(&conn, broker_addr, DEFAULT_BROKER_PORT,
        3 * CLOCK_SECOND,
        MQTT_CLEAN_SESSION_ON);
            
    state = STATE_CONNECTING;
}
/*------------------------------------*/
/*         MQTT STATE MACHINE         */
/*------------------------------------*/
/* Un'MQTT client puo trovarsi in diversi stati, che dipendono
    principalmente dagli eventi che arrivano dall istanza MQTT
    e dalle azioni che vengono compiute dal Client (questo nodo)
    Qui, a seconda di quale stato si trova il nodo, vengono fatte
    diverse cose */
static void mqtt_state_machine()
{
    // La variabile state e' globale
    switch (state)
    {
        case STATE_INIT:
            /* Inizializzazione */
            LOG_INFO("Init phase\n");
            mqtt_register(&conn, &mqtt_client_process, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);
            state = STATE_NET_OK;
            /*-------------------*/
            break;

        case STATE_NET_OK:
            /* Connessione al Border Router riuscita */
            LOG_INFO("Connecting to Border Router\n");
            if(have_conn())
            {
                // Connect to broker
                connect_mqtt();
            }
            /*-------------------*/
            break;

        case STATE_CONNECTING:
            /* Connettendo all'MQTT Broker */
            LOG_INFO("Connecting\n");
        

            /*-------------------*/
            break;

        case STATE_CONNECTED:
            /* Connesso all'MQTT Broker */
            LOG_INFO("Connected\n");

            status = mqtt_subscribe(&conn, NULL, SUB_TOPIC, MQTT_QOS_LEVEL_0);

            // Errore coda piena
            if (status == MQTT_STATUS_OUT_QUEUE_FULL)
            {
                LOG_ERR("Comand queue was full!\n");
                // PROCESS_EXIT();
            }

            state = STATE_SUBSCRIBED;
            /*-------------------*/

            break;


        case STATE_SUBSCRIBED:
            /* Sottoscritto a un topic */

            setTimeStamp();
            simulate_temperature();

            snprintf(pub_buffer, sizeof(pub_buffer), "{\"tyre\":%d,\"temperature\":%d,\"timestamp\":%s}", ID_PAIR, temperature, timeStr);
            LOG_DBG("Invio: %s\n", pub_buffer);
            mqtt_publish (&conn, NULL, PUB_TOPIC, (u_int8_t *)pub_buffer, strlen(pub_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

            /*-------------------*/
            break;

        case STATE_DISCONNECTED:
            /* Disconnesso dal broker */
            LOG_INFO("Disconnected\n");
            ping_parent();
            state = STATE_NET_OK;
            /*-------------------*/
            break;

        case STATE_ERROR:
            /* Errore */
            LOG_INFO("Error\n");
            /*-------------------*/
            break;

        default:
            break;
    }

    // Resetto il timer della state machine
    etimer_reset(&periodic_state_timer);
    if(state == STATE_SUBSCRIBED) etimer_set(&periodic_state_timer, SAMPLING_FREQUENCE);

}

/*------------------------------------*/
/*            ENDLESS LOOP            */
/*------------------------------------*/
PROCESS_THREAD(mqtt_client_process, ev, data)
{
    PROCESS_BEGIN();
    LOG_INFO("Starting MQTT Client\n");
    client_init();

    while(1)
    {
        PROCESS_YIELD();

        // La state machine va avviata ogni STATE_MACHINE_TIMER
        if(ev == PROCESS_EVENT_TIMER && data == &periodic_state_timer)
        {
            mqtt_state_machine();
        }
    }

    PROCESS_END();
}
/*------------------------------------*/