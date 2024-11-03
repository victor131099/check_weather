# Weather Report Service

## Introduction

The Weather Report Service is a Java Spring Boot application designed to fetch weather descriptions based on user-provided city and country inputs. The application integrates with the OpenWeatherMap API and enforces API key-based access control with rate limiting. Built with modern practices in mind, it follows an MVC architecture, satisfies Object-Oriented Programming (OOP) and SOLID principles, and has robust exception handling to ensure reliability.

## Design and Implementation

This project uses Spring WebFlux and R2DBC, allowing the service to handle high traffic and scale effectively through asynchronous, non-blocking operations. The WebClient is used for efficient external API calls, supporting reactive programming principles across all layers. This approach prepares the service for production-level demands, enabling it to handle high concurrency gracefully.

Key aspects of the design include:
- **MVC Pattern**: The project structure is organized around the Model-View-Controller pattern, ensuring clear separation of concerns.
- **Reactive Programming**: By using Spring WebFlux and R2DBC, the application is fully asynchronous, improving performance and responsiveness.
- **API Key Enforcement & Rate Limiting**: The service allows up to 5 requests per hour per API key, returning a suitable error response if this limit is exceeded.
- **Exception Handling**: Detailed and consistent exception handling provides clear error messages, aiding in debugging and maintaining high reliability.

## Technology Stack

- **Java 17**
- **Spring Boot**
- **Spring WebFlux**
- **R2DBC** (for non-blocking database access)
- **WebClient** (for external API calls)
- **H2 Database** (for in-memory data persistence)
- **Gradle** (for dependency management)

## Testing and Code Coverage

- **TDD Discipline**: This project adheres to Test-Driven Development (TDD) principles, with comprehensive test coverage and code quality checking.
- **Types of Tests**: The tests include unit, component, and integration tests to verify functionality, stability, and compliance with the service's requirements.
- **Code Coverage**: High code coverage is maintained across different components, ensuring the reliability and robustness of the application.

## Setup and Execution

1. **Clone the Repository**:
2. **Running app in local environment**
3. **Using the following postman request to calling service: curl --location 'http://localhost:8080/api/weather?city=Ohio&country=gb'"
