#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uiplib.h"


#include "sys/ctimer.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

#include "sys/etimer.h"
#include "dev/leds.h"
#include "dev/button-hal.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Wheel Actuator"
#define LOG_LEVEL LOG_LEVEL_DBG


extern coap_resource_t res_wheel_led;

static struct etimer periodic_state_timer;
#define STATE_TIMER (CLOCK_SECOND * 5)

static bool command = false;
static char client_id[40];
static char toSend[100];

static bool have_conn (void)
{
    if (uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL)
    {
        return false;
    }

    return true;
}

// Gestione registrazione al Cloud
void handler (coap_message_t *response)
{
    const uint8_t *chunk;

    if (response != NULL)
    {
        int len = coap_get_payload(response, &chunk);
        LOG_INFO ("%.*s\n", len, (char*)chunk);
        isRegistred = true;
    }
    else
    {
        LOG_ERR("ERROR: Actuator not subcrived");
    }
}


PROCESS(coap_server_wheel_leds, "Driver Wheel Led Actuator");
AUTOSTART_PROCESSES(&coap_server_wheel_leds);

PROCESS_THREAD(coap_server_wheel_leds, ev, data)
{
    PROCESS_BEGIN();

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    coap_activate_resource(&res_wheel_led, "Wheel Led");

    etimer_set(&periodic_state_timer, STATE_TIMER);
    coap_endpoint_parse(SERVER_IP, strlen(SERVER_IP), &server_ep);

    while(1)
    {
        PROCESS_WAIT_EVENT();
        if(ev == PROCESS_EVENT_TIMER && data == &periodic_state_timer)
        {
            // Registra
            if(have_conn())
            {
                if(isRegistred == 0)
                {
                    uip_ds6_addr_t *global_addr = uip_ds6_get_global(ADDR_PREFERRED);

                    sprintf(client_id, "%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x",
                        global_addr->ipaddr.u8[0], global_addr->ipaddr.u8[1],
                        global_addr->ipaddr.u8[2], global_addr->ipaddr.u8[3],
                        global_addr->ipaddr.u8[4], global_addr->ipaddr.u8[5],
                        global_addr->ipaddr.u8[6], global_addr->ipaddr.u8[7],
                        global_addr->ipaddr.u8[8], global_addr->ipaddr.u8[9],
                        global_addr->ipaddr.u8[10], global_addr->ipaddr.u8[11],
                        global_addr->ipaddr.u8[12], global_addr->ipaddr.u8[13],
                        global_addr->ipaddr.u8[14], global_addr->ipaddr.u8[15]);
                    
                    int leng = sprintf(toSend,"type=REG&tyre_position=%d&addr=%s", TYRE, client_id);

                    coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
                    coap_set_header_uri_path(request, "registrator");
                    coap_set_payload(request, toSend, leng);

                    printf("Sending registration request...\n");
                    COAP_BLOCKING_REQUEST(&server_ep, request, handler);
    
                }
                // Check if still registered
                else
                {
                    // TO DO
                }
            }
            else
            {
                LOG_DBG("Connecting to Border Router\n");
            }

            etimer_reset(&periodic_state_timer);
        }
    }

    PROCESS_END();
}