Title: Control Flow Statements
Objective: Understand if-else, switch, loops, and branching.

## Explanation

Control flow statements determine the order in which code executes.

Conditionals:
- `if`, `if-else`, `if-else if-else`
- `switch` (works with int, String, enum)

Loops:
- `for` (definite iteration)
- `while` (condition-based)
- `do-while` (executes at least once)

Branching:
- `break` (exit loop/switch)
- `continue` (skip to next iteration)
- `return` (exit method)

## Example

```java
// If-else
int score = 85;
if (score >= 90) {
    System.out.println("A");
} else if (score >= 80) {
    System.out.println("B");
} else {
    System.out.println("C");
}

// Switch
String day = "MONDAY";
switch (day) {
    case "MONDAY": System.out.println("Start of week"); break;
    case "FRIDAY": System.out.println("Weekend near"); break;
    default: System.out.println("Regular day");
}

// For loop
for (int i = 0; i < 5; i++) {
    System.out.print(i + " ");  // 0 1 2 3 4
}
```

## Real World Example

Banking applications use `if-else` for transaction validation (sufficient balance?), `switch` for handling different transaction types (DEPOSIT, WITHDRAWAL, TRANSFER), and `for` loops for processing batch transactions. Gaming applications use `while` loops for the main game loop.

## Summary

Control flow structures give your programs decision-making and repetition capabilities. Choose the right structure: `if-else` for boolean conditions, `switch` for multi-way branches, `for` for counted iterations, `while` for condition-based loops.

## Practice

1. Write a program that prints numbers 1 to 10 using each loop type
2. Create a grade calculator with if-else (A: 90+, B: 80+, C: 70+, D: 60+, F: below)
3. Use a switch statement for a simple calculator (+ - * /)
4. Write a nested loop to print a multiplication table