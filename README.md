# Trade Reconciliation Service

Automated Trade Data Reconciliation Microservice for financial institutions, trading platforms, and data management teams.

## Project Overview

The Trade Reconciliation Service is a robust and scalable backend microservice designed to automate the reconciliation of trade data originating from disparate financial systems. This microservice enhances data integrity, improves operational efficiency, and provides a foundation for reliable reporting and analysis.

### Key Features

- **Asynchronous Trade Data Ingestion**: Receive trade data from multiple upstream systems via RabbitMQ
- **Automated Reconciliation**: Compare trade data from different systems and identify discrepancies
- **RESTful API**: Submit trade data and retrieve reconciliation status
- **Configurable Matching Criteria**: Flexible configuration for trade matching rules
- **Timeout Handling**: Automatic handling of pending reconciliations that exceed the timeout threshold
- **Comprehensive Logging**: Detailed logging of all operations for audit and troubleshooting
- **Error Resilience**: Robust error handling with detailed error reporting

## Technologies Used

- **Java 17**: Latest LTS version of Java
- **Spring Boot 3.2.3**: Modern Java framework for building microservices
- **RabbitMQ**: Message broker for asynchronous processing
- **PostgreSQL**: Relational database for data storage
- **Docker & Docker Compose**: Containerization and orchestration
- **Maven**: Dependency management and build tool
- **Jenkins**: CI/CD pipeline automation
- **JUnit 5 & TestContainers**: Comprehensive testing framework

## Project Structure

```
trade-reconciliation-service/
├── src/
│   ├── main/
│   │   ├── java/com/trading/reconciliation/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Exception handling
│   │   │   ├── messaging/       # RabbitMQ message listeners
│   │   │   ├── model/           # Entity models
│   │   │   ├── repository/      # Database repositories
│   │   │   ├── service/         # Business logic services
│   │   │   └── TradeReconciliationApplication.java
│   │   └── resources/
│   │       └── application.yml  # Application configuration
│   └── test/
│       └── java/com/trading/reconciliation/
│           ├── integration/     # Integration tests
│           └── service/         # Unit tests
├── .mvn/wrapper/                # Maven wrapper
├── docker-compose.yml           # Docker Compose configuration
├── Dockerfile                   # Docker image definition
├── Jenkinsfile                  # CI/CD pipeline definition
├── mvnw                         # Maven wrapper script
├── pom.xml                      # Maven project definition
└── README.md                    # Project documentation
```

## Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6 or later
- Docker and Docker Compose
- Git

### Local Development Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd trade-reconciliation-service
   ```

2. Build the application:
   ```bash
   ./mvnw clean package
   ```
   
   Note: If you encounter permission issues with the Maven wrapper script, make it executable:
   ```bash
   chmod +x mvnw
   ```

3. Start the application with Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. The service will be available at http://localhost:8080

### Development Workflow

If you're developing without Docker, you can run:

```bash
# Run PostgreSQL and RabbitMQ using Docker
docker-compose up -d postgres rabbitmq

# Run the application locally
./mvnw spring-boot:run
```

### Running Tests

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify -DskipUnitTests
```

### Environment Variables

The following environment variables can be configured:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| POSTGRES_URL | PostgreSQL connection URL | jdbc:postgresql://localhost:5432/trade_reconciliation |
| POSTGRES_USER | PostgreSQL username | postgres |
| POSTGRES_PASSWORD | PostgreSQL password | postgres |
| RABBITMQ_HOST | RabbitMQ host | localhost |
| RABBITMQ_PORT | RabbitMQ port | 5672 |
| RABBITMQ_USERNAME | RabbitMQ username | guest |
| RABBITMQ_PASSWORD | RabbitMQ password | guest |
| RABBITMQ_VHOST | RabbitMQ virtual host | / |
| RECONCILIATION_TIMEOUT_MINUTES | Timeout for pending reconciliations | 60 |

## API Documentation

### Trade Submission API

#### Submit Trade from System A

```
POST /trades/systemA
```

Request Body:
```json
{
  "tradeId": "T123456",
  "instrument": "AAPL",
  "quantity": 100,
  "price": 150.75,
  "tradeDate": "2023-06-15T10:30:00",
  "counterparty": "BROKER_A"
}
```

Response: HTTP 202 (Accepted)

#### Submit Trade from System B

```
POST /trades/systemB
```

Request Body: Same format as System A

Response: HTTP 202 (Accepted)

### Reconciliation Status API

#### Get Reconciliation Status for a Trade

```
GET /reconciliations/{tradeId}
```

Response:
```json
{
  "tradeId": "T123456",
  "status": "MATCHED",
  "statusDescription": "Matched",
  "details": "Trades matched successfully",
  "createdAt": "2023-06-15T10:35:00",
  "updatedAt": "2023-06-15T10:35:05",
  "lastReconciliationAttempt": "2023-06-15T10:35:05"
}
```

#### Get List of Reconciliations

```
GET /reconciliations?status=MISMATCHED&page=0&size=20
```

Parameters:
- `status` (optional): Filter by reconciliation status (PENDING, MATCHED, MISMATCHED, RECONCILIATION_TIMEOUT, ERROR)
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Page size (default: 20)

Response: Paginated list of reconciliation status objects

#### Manually Trigger Reconciliation

```
POST /reconciliations/{tradeId}/trigger
```

Response: HTTP 202 (Accepted)

## Architecture

The service follows a layered architecture:

1. **Controller Layer**: REST API endpoints
2. **Service Layer**: Business logic and reconciliation processing
3. **Repository Layer**: Data access and persistence
4. **Messaging Layer**: Asynchronous communication with RabbitMQ

### Data Flow

1. Trade data is received via REST API or directly from message queues
2. Raw trade data is stored in the database
3. Reconciliation tasks are queued for asynchronous processing
4. Reconciliation worker compares trade data from different systems
5. Reconciliation results are stored in the database
6. Clients can query reconciliation status via REST API

### Reconciliation Process

1. When trade data is received from either System A or System B, it's stored in the database
2. A reconciliation task is then queued for processing
3. The reconciliation service compares trade data from both systems for the same trade ID
4. If data from both systems is available, fields are compared and discrepancies are identified
5. If data from only one system is available, the status is set to PENDING
6. If a reconciliation remains in PENDING status for longer than the configured timeout, it's marked as RECONCILIATION_TIMEOUT
7. The reconciliation status and details are stored in the database for future queries

## CI/CD Pipeline

The project includes a Jenkinsfile that defines the CI/CD pipeline:

1. Code checkout from Git
2. Build with Maven
3. Run unit tests
4. Run integration tests
5. Package the application
6. Build Docker image
7. Deploy to development environment

### Running the Pipeline Locally

You can simulate the CI/CD pipeline locally with the following commands:

```bash
# Checkout and build
./mvnw clean compile

# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify -DskipUnitTests

# Package the application
./mvnw package -DskipTests

# Build and deploy with Docker
docker build -t trade-reconciliation-service:latest .
docker-compose up -d
```

## Troubleshooting

### Common Issues

1. **RabbitMQ Connection Issues**:
   - Check if RabbitMQ is running: `docker ps | grep rabbitmq`
   - Verify connection settings in application.yml

2. **Database Connection Issues**:
   - Check if PostgreSQL is running: `docker ps | grep postgres`
   - Verify database connection settings in application.yml

3. **Application Won't Start**:
   - Check logs: `docker logs trade-reconciliation-service`
   - Verify all environment variables are set correctly

4. **Tests Failing**:
   - Ensure Docker is running for integration tests
   - Check test logs for specific errors

### Logs

Logs are stored in the `logs` directory and include detailed information about application operations, errors, and reconciliation processes.

## Future Enhancements

- Add support for more source systems
- Implement more sophisticated matching algorithms
- Add user authentication and authorization
- Implement a web-based dashboard for monitoring reconciliation status
- Add support for real-time notifications of reconciliation results
- Implement a dead-letter queue for failed reconciliation attempts
- Add metrics and monitoring with Prometheus and Grafana

## Security Considerations

- The current implementation does not include authentication or authorization
- In a production environment, consider implementing:
  - API key or OAuth2 authentication
  - HTTPS for secure communication
  - Role-based access control
  - Data encryption for sensitive information

## Performance Considerations

- The service is designed to handle high volumes of trade data
- For extremely high throughput scenarios, consider:
  - Scaling the service horizontally
  - Optimizing database queries
  - Implementing caching for frequently accessed data
  - Using a more performant message broker configuration

## Known Issues

- None at this time

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 