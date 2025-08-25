# Mplex Demo Project in Spring Boot

This project is a demonstration of a Spring Boot application simulating key functionalities of the Modaplex software. It
serves as a simplified, backend-focused example of the original JavaFX-based desktop application, designed to showcase
proficiency in Spring Boot and to illustrate a transition from desktop to web-based architecture.

## Project Structure

The project is organized into the following main components:

**client**

- The main application that contains the logic for controlling the laboratory device.
- it runs on [http://localhost:8090](http://localhost:8090).
- The application includes a **service client**, which is used by a service technician for maintenance or control of the
  device. Log in credentials:
    - Username: `service`
    - Password: `service`

**emulator**

- Application for simulating the hardware environment for testing purposes.
- It runs on [http://localhost:8091](http://localhost:8091).
- It allows the software to be tested without needing the physical device.

**device (deprecated)**

- This was a shared module that acted as interface between the client and the emulator.
- It was initially designed to handle both **_synchronous commands and asynchronous events via one WebSocket_**
  connection.
- this was replaced by the `rest-api` and `websocket` modules which separate the two communation ways. They
  make use of a microservice approach and they use Spring Boot features like STOMP.
- technologies:
    - Jackson for JSON binding
    - Spring Events for receiving events from the device
    - org.springframework.web.socket for WebSocket server connection in the emulator

**rest-api**

- A shared module that defines a REST API communication between the client and the emulator. The client sends a
  request to the emulator, waits while the emulator processes the request and sends a response back. This is
  synchronous.
- it does the work of the former commands in the `device` module.
- technologies:
    - openapi-generator for generating the API commands on the client and emulator side
    - Spring REST client for handling the API commands in the client
- advantages:
    - Ensures that both the client and emulator are in sync with the API definitions.
    - Reduces boilerplate code by generating the necessary classes and interfaces.
    - Benefits of REST for Commands: By adopting HTTP REST for the synchronous calls, we align with a common pattern for
      microservices public APIs (client-to-service calls). HTTP/JSON is human-readable (easy to log and inspect), and
      tools like Postman or Swagger-UI can be used to test the emulator’s API. We’ve effectively externalized the device
      commands as a service API, which is easier to work with than an opaque WebSocket protocol. Additionally, we let
      Spring handle a lot of work (threads, JSON binding, error handling via exceptions), so we write less custom code.

**websocket**

- the module includes STOMP via WebSocket
- it lets the emulator send asynchronous events to the client
- it publishes the events to the destination "/topic/{{subsystem}}.{{topic}}" ie "topic/fluidics.errors"
-

## Current State

- The WebSocket connects when the client sends a command.
- The emulator can send events and receive commands.
- The emulator features a log page where all commands and events are recorded.
- Users can log in as a service technician in the client (Username + Password: `service`)

## Technologies Used

- **Java 21**: The primary programming language.
- **OpenAPI Generator**: For generating API client and server code from OpenAPI specifications.
- **Spring Boot**: For building the RESTful backend services:
    - **_Spring Security_** for logging in as service technician in client (Username + Password: `service`)
    - **_Spring REST client_** for synchronous commands from client to emulator.
    - **_STOMP via Spring WebSocket_** for asynchronous events from emulator to client.
    - _deprecated_ (used by device):
        - **_Spring Events_** for receiving events from the device.
        - **_org.springframework.web.socket_** for WebSocket server connection in the emulator.
        - **_Jackson_** for device interface API.
- **Lombok** for reducing boilerplate code.
- **JUnit**: For unit testing the components.
- **Maven**: For project management and dependency resolution.

## Getting Started

- Ensure that JDK 21 or higher is installed and that it is set as your JAVA_HOME

1. Clone the repository:
    ```bash
    git clone https://github.com/cmiethling/mplex_demo_app.git
    ```  
2. Navigate to the project directory:
   ```bash
   cd mplex_demo_app
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

#### Running the Applications:

Start the Spring Boot applications using the script:

```bash
./start-all.sh
```

- The client will start on http://localhost:8090.
- The emulator will start on http://localhost:8091.
- Stop the apps by pressing `Ctrl + C`

#### Running Tests

Execute the unit tests using Maven:

```bash
mvn test
```

## Project Scope

This demo project is an ongoing effort and future updates may include additional features.