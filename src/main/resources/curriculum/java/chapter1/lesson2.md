Title: Variables and Data Types
Objective: Learn about primitive types, variables, and type conversion.

## Explanation

Java has 8 primitive data types. Variables are containers that hold values of a specific type.

Primitive types:
- `byte` (8-bit, -128 to 127)
- `short` (16-bit, -32,768 to 32,767)
- `int` (32-bit, -2^31 to 2^31-1)
- `long` (64-bit, -2^63 to 2^63-1)
- `float` (32-bit floating point)
- `double` (64-bit floating point)
- `char` (16-bit Unicode character)
- `boolean` (true or false)

Variables must be declared before use. Type conversion can be implicit (widening) or explicit (narrowing/casting).

## Example

```java
int age = 25;
double price = 19.99;
char grade = 'A';
boolean isActive = true;
long population = 8_000_000_000L;
float pi = 3.14f;

// Type conversion
double d = age;        // implicit widening
int i = (int) price;   // explicit narrowing (truncates to 19)
```

## Real World Example

Banking applications use `BigDecimal` for monetary values (not `double` due to precision), `long` for transaction IDs, and `boolean` for account status flags. E-commerce platforms use `int` for quantities, `double` for discounts, and `String` (reference type) for product names.

## Summary

Java provides 8 primitive types for basic values. Type conversion can be implicit (widening) or explicit (casting). Choose the appropriate type based on the range and precision your application needs.

## Practice

1. Declare variables for your name, age, height, and student status
2. Try assigning an `int` value to a `double` variable (implicit)
3. Try assigning a `double` to an `int` variable with casting
4. Experiment with overflow: what happens when you add 1 to `Integer.MAX_VALUE`?