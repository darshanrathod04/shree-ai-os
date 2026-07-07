Title: Request Handling
Objective: Handle path variables, query params, and request bodies.

## Explanation

Spring MVC provides annotations to extract data from HTTP requests.

Request data sources:
- `@PathVariable`: Extract from URL path `/users/{id}`
- `@RequestParam`: Extract query parameters `/users?page=1`
- `@RequestBody`: Bind request body to object
- `@RequestHeader`: Extract HTTP headers
- `@CookieValue`: Extract cookie values

Default values and optional parameters are supported.

## Example

```java
@GetMapping("/search")
public List<User> searchUsers(
    @RequestParam(required = false, defaultValue = "0") int page,
    @RequestParam int size,
    @RequestParam(required = false) String name
) {
    return userService.search(page, size, name);
}

@PostMapping
public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    User saved = userService.save(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

## Real World Example

E-commerce APIs use `@RequestParam` for pagination (`?page=1&size=20`), `@PathVariable` for product IDs (`/products/123`), and `@RequestBody` for order creation with complex JSON payloads.

## Summary

Use `@PathVariable` for required path segments, `@RequestParam` for optional query parameters, and `@RequestBody` for POST/PUT payloads. Always validate request bodies with `@Valid`.

## Practice

1. Add pagination to the BookController using @RequestParam
2. Add a search endpoint that accepts query parameters
3. Create a POST endpoint with @Valid annotation
4. Add custom validation error messages