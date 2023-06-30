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
#define LOG_MODULE "Wheel Actuator"
#define LOG_LEVEL LOG_LEVEL_DBG

extern coap_resource_t res_wheel_led_toggle;

PROCESS(coap_server_wheel_leds, "Driver Wheel Led Actuator");
AUTOSTART_PROCESSES(&coap_server_wheel_leds);

PROCESS_THREAD(coap_server_wheel_leds, ev, data)
{
    PROCESS_BEGIN();

    coap_activate_resource(&res_wheel_led_toggle, "Wheel Led");
    leds_on(LEDS_RED);

    while(1)
    {
        PROCESS_WAIT_EVENT();
    }

    PROCESS_END();
}