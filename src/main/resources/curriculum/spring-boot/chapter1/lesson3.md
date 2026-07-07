Title: Spring Boot Auto-Configuration
Objective: Learn how Spring Boot simplifies configuration.

## Explanation

Spring Boot auto-configuration automatically configures Spring beans based on dependencies in the classpath. It uses `@EnableAutoConfiguration` and conditionals like `@ConditionalOnClass` and `@ConditionalOnMissingBean`.

Key annotations:
- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`
- `@ConditionalOnClass`: Configure if a class is present
- `@ConditionalOnProperty`: Configure based on property values
- `@ConditionalOnMissingBean`: Configure if no custom bean exists

## Example

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: secret
  jpa:
    hibernate:
      ddl-auto: update
```

Spring Boot sees `spring-boot-starter-data-jpa` on the classpath and auto-configures DataSource, EntityManagerFactory, and JpaTransactionManager.

## Real World Example

When adding `spring-boot-starter-web`, Spring Boot auto-configures Tomcat, Jackson, and Spring MVC. Adding `spring-boot-starter-security` automatically adds security filters and a login page.

## Summary

Auto-configuration reduces boilerplate by intelligently configuring beans based on classpath dependencies and properties. Customize behavior through `application.properties` or `application.yml`.

## Practice

1. Check what gets auto-configured with `spring-boot-starter-web`
2. Override the default server port using `server.port=9090`
3. Disable a specific auto-configuration using `@EnableAutoConfiguration(exclude=...)`
4. Add a custom DataSource bean and see auto-configuration back off