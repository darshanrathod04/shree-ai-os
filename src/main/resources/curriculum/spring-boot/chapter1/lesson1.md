Title: Introduction to Spring Framework
Objective: Understand Spring architecture and modules.

## Explanation

Spring Framework is a comprehensive framework for enterprise Java development. It provides infrastructure support for dependency injection, aspect-oriented programming, data access, and web applications.

Core modules:
- Spring Core: IoC container and dependency injection
- Spring MVC: Web framework for REST APIs
- Spring Data: Database access abstraction
- Spring Security: Authentication and authorization
- Spring Boot: Auto-configuration and production-ready features

## Example

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

This single annotation enables auto-configuration, component scanning, and embedded server.

## Real World Example

Netflix, Amazon, and Microsoft use Spring Boot for microservices. A typical enterprise deployment has dozens of Spring Boot services handling authentication, payment processing, inventory management, and analytics.

## Summary

Spring Framework provides a modular ecosystem for enterprise Java development. Spring Boot simplifies configuration and enables rapid application development with embedded servers and auto-configuration.

## Practice

1. Create a new Spring Boot project using Spring Initializr
2. Add the Web dependency
3. Run the application and verify it starts on port 8080
4. Access http://localhost:8080/actuator/health