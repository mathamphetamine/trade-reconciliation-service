# Contributing to Trade Reconciliation Service

Thank you for your interest in contributing to the Trade Reconciliation Service! This document provides guidelines and instructions for contributing to this project.

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct, which requires respectful and professional interaction with all project contributors.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR-USERNAME/trade-reconciliation-service.git`
3. Create a branch for your changes: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Run tests: `./mvnw verify`
6. Commit your changes: `git commit -m "Add feature X"`
7. Push to your fork: `git push origin feature/your-feature-name`
8. Create a pull request

## Development Environment

### Prerequisites

- Java 17 or later
- Maven 3.6 or later
- Docker and Docker Compose
- Git

### Local Development Setup

1. Clone the repository
2. Build the application: `./mvnw clean package`
3. Start the dependencies: `docker-compose up -d postgres rabbitmq`
4. Run the application: `./mvnw spring-boot:run`

## Coding Standards

### Java

- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add JavaDoc comments to all public methods
- Keep methods small and focused on a single task
- Use proper exception handling

### Testing

- Write unit tests for all new code
- Aim for a code coverage of at least 80%
- Write integration tests for complex functionality
- Tests should be independent and not rely on execution order

### Commit Messages

- Use the imperative mood: "Add feature" instead of "Added feature"
- Start with a capital letter
- Keep the first line under 50 characters
- Reference issue numbers when applicable: "Fix #123: Add validation"

## Pull Request Process

1. Update the README.md or documentation with details of changes, if applicable
2. Update the version numbers in any examples or documentation following semantic versioning
3. Make sure all tests pass
4. Get at least one code review from a maintainer
5. A maintainer will merge the pull request once it meets these criteria

## Reporting Bugs

When reporting bugs, please include:

- A clear and descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Environment details (OS, Java version, etc.)

## Feature Requests

When requesting features, please include:

- A clear and descriptive title
- A detailed description of the feature
- Why this feature would be beneficial
- Any examples or mockups, if applicable

## License

By contributing to this project, you agree that your contributions will be licensed under the project's MIT License. 