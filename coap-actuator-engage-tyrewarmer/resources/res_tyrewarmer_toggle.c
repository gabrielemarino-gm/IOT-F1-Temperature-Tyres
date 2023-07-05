#include <stdlib.h>
#include <string.h>

#include "dev/leds.h"
#include "coap-engine.h"


/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Tyrewarmer-Resource"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler(
    coap_message_t *request,
    coap_message_t *response,
    uint8_t *buffer,
    uint16_t preferred_size, int32_t *offset
);

static void res_put_handler(
    coap_message_t *request,
    coap_message_t *response,
    uint8_t *buffer,
    uint16_t preferred_size, int32_t *offset
);

static void res_trigger(void);

EVENT_RESOURCE(res_tyrewarmer_toggle,
        "title=\"Tyrewarmer manager\"",
        res_get_handler,
        NULL,
        res_put_handler,
        NULL,
        res_trigger
);

static int status = -1; // -1 ForceStop, 0 offBecauseTemp, 1 onBecauseTemp

// Red led -> The Tyrewarmer is manually deactivated (Button)
// Yellow led -> The tyrewarmer is active but not working becouse temp > threshold
// Green led -> The tyrewarmer is currently active

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int length = 0;
  const char* message;

  switch (status)
  {
    case -1:
      length = 8;
      message = "DISABLED";
      break;
    case 0:
      length = 13;
      message = "ENABLED - OFF";
      break;
    case 1:
      length = 12;
      message = "ENABLED - ON";
      break;

    default:
    message = "ERROR";
      break;
  }

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

static void
res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  size_t len = 0;
  const char *command = NULL;
  uint8_t led = 0;
  int success = 1;

  if((len = coap_get_query_variable(request, "command", &command)))
  {
    LOG_DBG("Command %.*s\n", (int)len, command);

    // DISATTIVO la termocoperta
    if(strncmp(command, "DISABLE", len) == 0)
    {
      led = LEDS_RED;
      status = -1;
    }

    // ATTIVO la termocoperta
    else if(strncmp(command, "ENABLE", len) == 0)
    {
      // Ma solo se era DISATTIVATA
      if (status < 0)
      {
        led = LEDS_GREEN;
        status = 1;
      } 
    }

    // SPENGO la termocoperta
    else if(strncmp(command, "HIGHTEMP", len) == 0)
    {
      // Ma solo se non era DISATTIVATA
      if(status == -1)
        success = 0;
      else
      {
        led = LEDS_YELLOW; 
        status = 0;
      }
    }

    // ACCENDO la termocoperta
    else if(strncmp(command, "LOWTEMP", len) == 0)
    {
      // Ma solo se non era DISATTIVATA
      if(status == -1) 
        success = 0;
      else
      {
        led = LEDS_GREEN;
        status = 1;
      }
    }
    else
    {
      success = 0;
    }
  }
  else
  {
    success = 0;
  }
    

  if(!success) {
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
  else
  {
    coap_set_status_code(response, CONTENT_2_05);
    leds_off(LEDS_ALL);
    leds_on(led);
  }
}

static void
res_trigger()
{
  uint8_t led = 0;

  // ATTIVO la termocoperta
  if(status < 0)
  {
    status = 1;
    led = LEDS_GREEN;
  }

  // DISATTIVO la termocoperta
  else
  {
    status = -1;
    led = LEDS_RED;
  }

  leds_off(LEDS_ALL);
  leds_on(led);

}
