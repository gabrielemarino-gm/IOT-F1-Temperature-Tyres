#include "contiki.h"

#include "sys/ctimer.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "RPL BR"
#define LOG_LEVEL LOG_LEVEL_INFO

/* Declare and auto-start this file's process */
PROCESS(contiki_ng_br, "Contiki-NG Border Router");
AUTOSTART_PROCESSES(&contiki_ng_br);

static struct ctimer polizia;

static void blinker()
{
  leds_toggle(LEDS_GREEN);
  ctimer_set(&polizia, CLOCK_SECOND, blinker, NULL);
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(contiki_ng_br, ev, data)
{
  PROCESS_BEGIN();

  ctimer_set(&polizia, CLOCK_SECOND, blinker, NULL);
  
#if BORDER_ROUTER_CONF_WEBSERVER
  PROCESS_NAME(webserver_nogui_process);
  process_start(&webserver_nogui_process, NULL);
#endif /* BORDER_ROUTER_CONF_WEBSERVER */

  LOG_INFO("Contiki-NG Border Router started\n");

  PROCESS_END();
}