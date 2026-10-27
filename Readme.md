# Mplex Demo App

A small Spring Boot demo of how a control software talks to a laboratory device. It is a simplified, web-based remake of
the communication in the Modaplex software (originally a JavaFX desktop application). Instead of real hardware, an
emulator plays the device.

## What it shows

- **Commands via REST, results as events:** the client sends a command (eg "gel pump on"), the device only confirms it
  (`202 Accepted` + `commandId`). The result follows asynchronously as event with the same `commandId`.
- **Events via STOMP over WebSocket:** state changes and errors of the device reach the client without polling.
- **Live browser update via Server-Sent Events:** the browser pages update themselves as soon as an event arrives.
- **API first:** the REST interface is defined in OpenAPI, client and server code are generated from it.
- **Device rules and errors:** invalid commands are rejected with `409 Conflict` as ProblemDetail (RFC 9457), eg the
  pump can't run while the valve is closed. A `GEL_PUMP_ERROR` switches the pump off and blocks it.
- **Robust connection:** the client reconnects 3 times (after 2, 4, 8 s) and shows the connection state. On every connect and
  reconnect the emulator sends its current states.

## Architecture

```mermaid
sequenceDiagram
    participant B as Browser
    participant C as Client :8090
    participant E as Emulator :8091
    Note over B, E: 1. Connect (also after every reconnect)
    C ->> E: STOMP subscribe /topic/FLUIDICS.states, ...
    E -->> C: current states
    Note over B, E: 2. Command
    B ->> C: Gel pump ON (service client)
    C ->> E: POST /restapi/fluidics/SetGelPump
    E -->> C: 202 Accepted {commandId}
    C -->> B: SSE update: "pending"
    Note over E: device executes the command (1 s)
    E -->> C: StatesEvent {gelPumpOn: true, commandId}
    C -->> B: SSE update: ON
```

If no result event arrives within 5 s, the client shows "no result from device".

## Modules

| Module              | Content                                                                                          |
|---------------------|--------------------------------------------------------------------------------------------------|
| `rest-api`          | OpenAPI specs of the commands, generates `client-api` (RestClient) and `server-api` (interfaces) |
| `device-events-api` | Event classes (eg `StatesEvent`, `ErrorEvent`) with their STOMP destination                      |
| `emulator`          | Plays the device: executes commands, sends events, web page to trigger errors                    |
| `client`            | Control software: status page, service client with commands (login required)                     |

## Technologies

Java 21, Spring Boot 3.5 (Web MVC, WebSocket/STOMP, Security, Thymeleaf), OpenAPI Generator, springdoc (Swagger UI),
Lombok, Bootstrap 5, Maven.

## Getting started

Requirements: JDK 21.

```bash
git clone https://github.com/chrisme86/mplex-demo-app.git
cd mplex-demo-app
mvn clean install
./start-all.sh        # stop with Ctrl + C
```

| URL                                   | What                                                      |
|---------------------------------------|-----------------------------------------------------------|
| http://localhost:8090                 | Client: current device states (read only)                 |
| http://localhost:8090/service_client  | Service client with commands, login `service` / `service` |
| http://localhost:8091                 | Emulator: device states, trigger errors                   |
| http://localhost:8091/swagger-ui.html | Swagger UI of the device REST API                         |

Things to try:

- Close the valve while the pump runs: `409 Conflict` "Gel pump is running".
- Set `GEL_PUMP_ERROR` in the emulator: the pump switches off and can't be switched on until the error is `NONE`.
- Stop the emulator: the client shows the reconnect attempts, after a restart "Reconnect" brings back the current
  states.
