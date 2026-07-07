Title: REST Controllers
Objective: Create REST endpoints with @RestController.

## Explanation

`@RestController` combines `@Controller` and `@ResponseBody`. It simplifies REST API development by eliminating the need to annotate each method with `@ResponseBody`.

Key annotations:
- `@GetMapping`: Handle GET requests
- `@PostMapping`: Handle POST requests
- `@PutMapping`: Handle PUT requests
- `@DeleteMapping`: Handle DELETE requests
- `@RequestMapping`: Base URL mapping at class level

## Example

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

## Real World Example

Netflix's API gateway routes to hundreds of microservices, each with REST controllers. A streaming service might have `MovieController`, `SeriesController`, `UserController`, and `SubscriptionController`.

## Summary

`@RestController` simplifies REST endpoint creation. Use `@XxxMapping` annotations for HTTP method binding. `@PathVariable` extracts path parameters, and `@RequestBody` binds request bodies.

## Practice

1. Create a `BookController` with CRUD endpoints
2. Return `ResponseEntity` with appropriate status codes
3. Add validation annotations to the request body
4. Test with Postman or curl