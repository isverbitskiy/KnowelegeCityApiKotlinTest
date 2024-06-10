# KnowledgeCityApiKotlinTest

KnowledgeCityApiKotlinTest is a project that demonstrates API testing using Kotlin and various testing frameworks. The
project focuses on testing a hypothetical QA system that handles user authentication, question retrieval, answer
submission, and score tracking.

## Features

- User login and registration tests
- Question retrieval and submission tests
- Answer correctness validation
- User score tracking and display
- Resetting user state for test preparation

## Prerequisites

- [Kotlin](https://kotlinlang.org/) 2.0.0
- [Gradle](https://gradle.org/) 8.5
- [JUnit](https://junit.org/junit5/) 5.10.2
- [Ktor](https://ktor.io/) 2.3.11
- [Allure](https://docs.qameta.io/allure/) 2.20.1 for reporting

## Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/isverbitskiy/KnowelegeCityApiKotlinTest.git
   cd KnowelegeCityApiKotlinTest

2. Open the project in your favorite IDE (IntelliJ IDEA recommended).
3. Build the project using Gradle:

   ```gradle build```

## Running Tests

To run the tests, use the following command:
```gradle clean test```

To generate an Allure report, run:
```gradle allure serve build/allure-results```
Open the generated report in your browser to view the detailed test results.

## Project Structure

      •  src/test/kotlin: Contains the test classes.
      •  build.gradle.kts: Gradle build script with dependencies and task configurations.build.gradle.kts: Gradle build script with dependencies and task configurations.

## Key Classes and Methods

### BaseApiTest

This class provides common functionality for API tests, such as sending requests and handling responses.

### LoginTest

Tests related to user login and registration.

### QuestionTest

Tests related to question retrieval and validation.

### AnswerTest

Tests related to answer submission and correctness validation.

### ScoreTest

Tests related to score tracking and display.

### ResetTest

Tests related to resetting the user state.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for review.

## Contact

For any inquiries or issues, please open an issue on GitHub.