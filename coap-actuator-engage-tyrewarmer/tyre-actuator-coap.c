#include "contiki.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "sys/ctimer.h"
#include "coap-engine.h"

#include "dev/leds.h"
#include "dev/button-hal.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Tyre Actuator"
#define LOG_LEVEL LOG_LEVEL_DBG

extern coap_resource_t 
    res_tyrewarmer_toggle;

PROCESS(coap_server, "Tyrewarmer actuator");
AUTOSTART_PROCESSES(&coap_server);

PROCESS_THREAD(coap_server, ev, data)
{
    PROCESS_BEGIN();

    coap_activate_resource(&res_tyrewarmer_toggle, "tyrewarmer");
    leds_on(LEDS_RED);

    while(1){
        PROCESS_YIELD();
        if(ev == button_hal_release_event)
        {
            LOG_DBG("*******BUTTON*******\n");
            
            // Evento bottone
            res_tyrewarmer_toggle.trigger();
        }
    }

    PROCESS_END();
}