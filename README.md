# IOT-F1-Temperature-Tyres

## Context 🏎️💨
The project aims to create an IoT-based system for monitoring Formula 1 tire temperatures, which are crucial for performance on the track. Sensors on each tire collect real-time temperature data and transmit it wirelessly to a central unit for analysis and visualization. This system helps maintain optimal grip and tire wear, allowing drivers to push their cars to the limit while staying in control.

## Tools
We utilize Contiki-NG for both the sensors and actuators in our system. Specifically, the sensors are implemented using the MQTT paradigm, while the actuators communicate with the centralized Java application through CoAP.
