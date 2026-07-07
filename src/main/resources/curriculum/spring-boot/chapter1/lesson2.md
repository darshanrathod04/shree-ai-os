Title: Inversion of Control and Dependency Injection
Objective: Master dependency injection patterns.

## Explanation

Inversion of Control (IoC) means the framework controls the flow of the program. Dependency Injection (DI) is a pattern where dependencies are provided to a class rather than created internally.

Spring provides:
- `@Component` for generic beans
- `@Service` for service layer beans
- `@Repository` for data access beans
- `@Controller` / `@RestController` for web controllers
- `@Autowired` for injecting dependencies
- Constructor injection (preferred) vs field injection

## Example

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    // Constructor injection (recommended)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

## Real World Example

E-commerce checkouts inject `PaymentService`, `InventoryService`, and `NotificationService` into an `OrderService`. Each service is independently testable. Spring manages their lifecycle and wiring.

## Summary

IoC inverts control from the application to the framework. DI injects dependencies through constructors. Constructor injection is preferred for immutability and testability.

## Practice

1. Create a `GreetingService` interface and implementation
2. Inject it into a controller using constructor injection
3. Test with different implementations
4. Add `@Primary` to designate a default implementation