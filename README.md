# ChatApp with WebSocket in Java

This is a simple Java application aimed at learning how WebSocket works in the context of Java. The application allows
users to connect to a WebSocket server by providing a JWT token obtained from the Spring Authorization Server. The
connection is established securely, and upon subscribing, the client's identity is verified to ensure it matches the
specified topic (notification-client).

## Getting Started

To use this application, you need to configure the authentication properties in the application.properties file:

- auth.server.uri=<authorization-server-ui>
- client-id=<your-client-id>
- client-secret=<your-client-secret>

## Prerequisites

- Spring Authorization Server: Set up and running. You can find the implementation
  here: [Spring Authorization Server](https://github.com/bogdanMierloiu/Spring-Authorization-Server-Implementation).
- Node.js ChatApp Interface: You can use my personal project as an interface. Find it
  here :[Chat App Node.js](https://github.com/bogdanMierloiu/ChatApp-with-Node.js).

## Features

- Connect to WebSocket server using JWT token obtained from Spring Authorization Server.
- Securely verify client identity upon subscribing.
- Simple and easy-to-understand implementation for learning purposes.

## Security Considerations

While WebSocket itself does not inherently provide security mechanisms, it is crucial to ensure the security of
connections established through WebSocket protocols. In this application, security measures have been implemented to
enhance the safety of WebSocket communications.

- Authentication and Authorization
Authentication and authorization are enforced through the use of interceptors in the 'configureClientInboundChannel'
method. This allows for the validation of tokens and verification of client identities before allowing them to establish
a connection or subscribe to topics.

## Contributing

Contributions are welcome! Feel free to open issues or pull requests for any improvements or suggestions.
