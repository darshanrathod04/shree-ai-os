Title: Operators and Expressions
Objective: Master arithmetic, logical, and comparison operators.

## Explanation

Operators in Java are special symbols that perform operations on operands.

Categories:
- Arithmetic: `+`, `-`, `*`, `/`, `%` (modulus)
- Relational: `==`, `!=`, `<`, `>`, `<=`, `>=`
- Logical: `&&` (AND), `||` (OR), `!` (NOT)
- Assignment: `=`, `+=`, `-=`, `*=`, `/=`, `%=`
- Unary: `++`, `--`, `+`, `-`
- Ternary: `condition ? value1 : value2`

Operator precedence determines evaluation order. Use parentheses for clarity.

## Example

```java
int a = 10, b = 3;
System.out.println(a + b);  // 13
System.out.println(a - b);  // 7
System.out.println(a * b);  // 30
System.out.println(a / b);  // 3 (integer division)
System.out.println(a % b);  // 1 (remainder)

boolean isAdult = age >= 18;
String status = (score >= 60) ? "Pass" : "Fail";

// Short-circuit evaluation
if (value != null && value.length() > 0) { }
```

## Real World Example

E-commerce checkout total calculation uses arithmetic operators. User authentication uses logical operators (`username.equals("admin") && password.equals("secret")`). Form validation uses relational operators to check minimum length, age requirements, and numeric ranges.

## Summary

Operators perform computations and comparisons. Understand precedence but use parentheses for readability. Short-circuit operators (`&&`, `||`) only evaluate the right side when necessary.

## Practice

1. Write a program that checks if a number is even or odd using `%`
2. Calculate the area of a circle: `PI * radius * radius`
3. Use the ternary operator to determine if a temperature is hot (>30) or cold
4. Experiment with `++x` vs `x++` and understand the difference