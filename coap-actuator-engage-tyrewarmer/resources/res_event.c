#include <stdio.h>
#include <string.h>
#include "coap-engine.h"
#include "coap.h"

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

EVENT_RESOURCE(res_event,
               "title=\"Shared counter\";obs",
               res_get_handler,
               NULL,
               NULL,
               NULL,
               res_event_handler);

/*
 * Use local resource state that is accessed by res_get_handler() and altered by res_event_handler() or PUT or POST.
 */
static int32_t event_counter = 0;

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_payload(response, buffer, snprintf((char *)buffer, preferred_size, "EVENT %lu\n", (unsigned long) event_counter));

  /* A post_handler that handles subscriptions/observing will be called for periodic resources by the framework. */
}

static void
res_event_handler(void)
{
    ++event_counter;

    coap_notify_observers(&res_event);
}
