#include <stdlib.h>
#include <string.h>

#include "coap-engine.h"

static void res_get_handler(
    coap_message_t *request,
    coap_message_t *response,
    uint8_t *buffer,
    uint16_t preferred_size, int32_t *offset
);

RESOURCE(res_test,
        "title=\"Test resource\"",
        res_get_handler,
        NULL,
        NULL,
        NULL
);

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    char const *const message = "This is a test resource";
    int length = 24;

    if(length < 0) {
      length = 0;
    }

    if(length > REST_MAX_CHUNK_SIZE) {
      length = REST_MAX_CHUNK_SIZE;
    }

    memcpy(buffer, message, length);
    
    coap_set_header_content_format(response, TEXT_PLAIN); /* text/plain is the default, hence this option could be omitted. */
    coap_set_header_etag(response, (uint8_t *)&length, 1);
    coap_set_payload(response, buffer, length);
}