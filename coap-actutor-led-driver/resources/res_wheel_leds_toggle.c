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

static void res_get_handler(
    coap_message_t *request, 
    coap_message_t *response, 
    uint8_t *buffer, 
    uint16_t preferred_size, 
    int32_t *offset);


static void res_put_post_handler(
    coap_message_t *request, 
    coap_message_t *response, 
    uint8_t *buffer, 
    uint16_t preferred_size, 
    int32_t *offset);

static int status = 0;

EVENT_RESOURCE(res_wheel_led,
        "title=\"Wheel Led Manager\"",
        res_get_handler,
        res_put_post_handler,
        res_put_post_handler,
        NULL,
        NULL
);


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    int length = 0;
    const char* message;

    switch (status)
    {
        case 0:
        length = 13;
        message = "CAR OFF TRACK";
        break;
        
        case 1:
        length = 12;
        message = "CAR ON TRACK";
        break;

        default:
        break;
    }

    if(length < 0) 
        length = 0;
    

    if(length > REST_MAX_CHUNK_SIZE) 
        length = REST_MAX_CHUNK_SIZE;

    memcpy(buffer, message, length);

    coap_set_header_content_format(response, TEXT_PLAIN); /* text/plain is the default, hence this option could be omitted. */
    coap_set_header_etag(response, (uint8_t *)&length, 1);
    coap_set_payload(response, buffer, length);
}


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
        
        // Gomma calda
        if (strncmp(command, "OVER", len) == 0)
            led = LEDS_RED;

        // Gomma fredda
        else if (strncmp(command, "UNDER", len) == 0)
            led = LEDS_YELLOW;

        // Gomma buona
        else if (strncmp(command, "GREAT", len) == 0)
            led = LEDS_GREEN; 
        
        // Aggiorna stato
        else if (strncmp(command, "UPDATE", len) == 0)
            status = (status == 0)? 1:0;
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