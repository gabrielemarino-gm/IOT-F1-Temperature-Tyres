#include "contiki.h"

#include "net/ipv6/uip-ds6.h"

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
#define LOG_MODULE "Tyre Actuator"
#define LOG_LEVEL LOG_LEVEL_DBG

extern coap_resource_t 
    res_tyrewarmer_toggle;

static struct etimer periodic_state_timer;
#define STATE_TIMER (CLOCK_SECOND * 5)

#define SERVER_IP "coap://[fd00::1]"
static coap_endpoint_t server_ep;
// static coap_message_t request[1];

static int isRegistered = 0;
static char client_id[64];

static bool have_conn(void)
{
    //Ritorna true solo se il nodo corrente ha un Public IP
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL)
    {
        return false;
    }
    return true;
}

void 
handler(coap_message_t *response){
    const uint8_t *chunk;

    if(response != NULL){
        int len = coap_get_payload(response, &chunk);
        printf("%.*s ", len, (char*)chunk);
    }
    else{
        printf("Error");
    }
}

PROCESS(coap_server, "Tyrewarmer actuator");
AUTOSTART_PROCESSES(&coap_server);

PROCESS_THREAD(coap_server, ev, data)
{
    PROCESS_BEGIN();

    coap_activate_resource(&res_tyrewarmer_toggle, "tyrewarmer");
    leds_on(LEDS_RED);

    etimer_set(&periodic_state_timer, STATE_TIMER);
    coap_endpoint_parse(SERVER_IP, strlen(SERVER_IP), &server_ep);

    while(1){
        PROCESS_YIELD();
        // Evento bottone
        if(ev == button_hal_release_event)
        {
            LOG_DBG("*******BUTTON*******\n");
            
            res_tyrewarmer_toggle.trigger();
        }
        else if(ev == PROCESS_EVENT_TIMER && data == &periodic_state_timer)
        {
            // Registra
            if(have_conn()){
                if(isRegistered == 0)
                {
                    snprintf(client_id, 64, "%02x%02x:%02x%02x:%02x%02x",
                        linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                        linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                        linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);
    
                    LOG_DBG("%s\n", client_id);
    
                }
                // Check if still registered
                else
                {

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