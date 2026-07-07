Title: Classes and Objects
Objective: Learn to define classes and create objects.

## Explanation

A class is a blueprint for creating objects. An object is an instance of a class with state (fields) and behavior (methods).

Key concepts:
- Class declaration with `class` keyword
- Fields store object state
- Methods define object behavior
- Constructors initialize objects
- `new` keyword creates instances
- `this` refers to the current instance

## Example

```java
public class Car {
    // Fields (state)
    String model;
    int year;
    boolean isRunning;

    // Constructor
    public Car(String model, int year) {
        this.model = model;
        this.year = year;
        this.isRunning = false;
    }

    // Methods (behavior)
    public void start() {
        isRunning = true;
        System.out.println(model + " started");
    }

    public void stop() {
        isRunning = false;
        System.out.println(model + " stopped");
    }

    public static void main(String[] args) {
        Car myCar = new Car("Tesla Model 3", 2024);
        myCar.start();
    }
}
```

## Real World Example

E-commerce systems model `Product`, `Customer`, `Order`, and `Cart` as classes. Each has fields (price, name, email) and methods (addToCart(), checkout(), calculateTotal()). Banking systems model `Account`, `Transaction`, and `Customer` classes with appropriate behaviors.

## Summary

Classes define the structure and behavior of objects. Use constructors for initialization, fields for state, and methods for behavior. The `new` keyword creates object instances in heap memory.

## Practice

1. Create a `Student` class with name, age, and grade fields
2. Add a method that prints "studying..." when called
3. Create a `Book` class with title, author, and ISBN
4. Create multiple objects and call their methods