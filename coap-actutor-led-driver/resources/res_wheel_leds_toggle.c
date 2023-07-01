#include "contiki.h"

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
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_put_post_handler(
    coap_message_t *request, 
    coap_message_t *response, 
    uint8_t *buffer, 
    uint16_t preferred_size, 
    int32_t *offset);


EVENT_RESOURCE(res_wheel_led,
        "title=\"Wheel Led Manager\"",
        NULL,
        res_put_post_handler,
        res_put_post_handler,
        NULL,
);


// Red led -> Tyres are overhiting
// Yellow led -> Tyres are cooling
// Green led -> Tyres are great

static void res_put_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    size_t len = 0;
    const char *command = NULL;
    uint8_t led = 0;
    int success = 1;

    if((len = coap_get_query_variable(request, "command", &command)))
    {
        LOG_DBG("Temp Tyres %.*s\n", (int)len, command);

        int command_int = atoi(command);
        LOG_DBG("Temp Tyres INT %.*d\n", (int)len, command_int);
        
        // Gomma calda
        if (strncmp(command, "OVER", len) == 0)
            led = LEDS_RED;
        
        // Gomma fredda
        else if (strncmp(command, "UNDER", len) == 0)
            led = LEDS_YELLOW;

        // Gomma buona
        else if (strncmp(command, "GREAT", len) == 0)
            led = LEDS_GREEN;    
    }
    else
    {
        success = 0;
    }

    if(!success) 
    {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
    else
    {
        coap_set_status_code(response, CONTENT_2_05);
        leds_off(LEDS_ALL);
        leds_on(led);
    }
}