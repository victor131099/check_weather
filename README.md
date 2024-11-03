# Weather Report Service

## Introduction

The Weather Report Service is a Java Spring Boot REST API application designed to fetch weather descriptions based on user-provided city and country inputs. The application integrates with the OpenWeatherMap API and enforces API key-based access control with rate limiting. Built with modern practices in mind, it follows an MVC architecture, satisfies Object-Oriented Programming (OOP) and SOLID principles, and has robust exception handling to ensure reliability.

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


## API Documentation and Usage
1. **API Endpoints**:
- **URL**: `/api/weather`
- **Method**: `GET`
- **Parameters**:
   - `city` (required): Name of the city to retrieve weather for.
   - `country` (optional): Name of the country to further specify the location.
   - `apiKey` (required): API key to access the service.
2. **Response**: Returns a JSON object with the weather description for the specified location.
3. **Errors**:
   - `400 BAD REQUEST`: For invalid or missing parameters.
   - `401 UNAUTHORIZED`: For invalid or missing API key.
   - `429 TOO MANY REQUESTS`: For exceeding the rate limit (5 requests per hour).
   - `503 SERVICE UNAVAILABLE`: For unhandled errors or issues with the external API.
4. **Example Request**:
   - `You can call by both city name or country code, Please refer to ISO 3166 (https://www.iso.org/obp/ui/#search) for the city names or country codes.
   - `You can specify the parameter not only in English. In this case, the API response should be returned in the same language as the language of requested location name if the location is in our predefined list of more than 200,000 locations.
5. **Example Request**:
   - `retrieve weather information for London, use the following example:`
```bash
curl -X GET "http://localhost:8080/api/weather?city=London&country=GB&apiKey={API_key}"