#include "contiki.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "sys/ctimer.h"
#include "coap-engine.h"


/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static struct ctimer clocker;

extern coap_resource_t 
    res_test,
    res_event;

static void callback(){
    res_event.trigger();
    ctimer_reset(&clocker);
}

PROCESS(personal_coap, "Personal CoAP Server");
AUTOSTART_PROCESSES(&personal_coap);

PROCESS_THREAD(personal_coap, ev, data)
{
    PROCESS_BEGIN();

    coap_activate_resource(&res_test, "test");
    coap_activate_resource(&res_event, "event");

    ctimer_set(&clocker, 2 * CLOCK_SECOND, callback, NULL);

    while(1){
        PROCESS_WAIT_EVENT();
    }

    PROCESS_END();
}