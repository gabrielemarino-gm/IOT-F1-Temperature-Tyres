#include "contiki.h"
#include "sys/etimer.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "coap-engine.h"
#include "coap-blocking-api.h"

#define SERVER_EP "coap://[fd00::1]"
#define INTERVAL (3 * CLOCK_SECOND)

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

PROCESS(personal_coap_client, "Personal CoAP Client");
AUTOSTART_PROCESSES(&personal_coap_client);

static struct etimer et;

PROCESS_THREAD(personal_coap_client, ev, data)
{
    PROCESS_BEGIN();
    static coap_message_t request[1];
    static coap_endpoint_t server_ep;

    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

    etimer_set(&et, INTERVAL);

    while(1)
    {
        PROCESS_YIELD();
        if(etimer_expired(&et)){
            coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
            coap_set_header_uri_path(request, "res1");
            printf("Sending request...\n");
            COAP_BLOCKING_REQUEST(&server_ep, request, handler);

            etimer_reset(&et);
        }
    }
    
  PROCESS_END();

}