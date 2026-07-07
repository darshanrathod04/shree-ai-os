Title: Inheritance and Polymorphism
Objective: Understand inheritance hierarchies and polymorphic behavior.

## Explanation

Inheritance allows a class to acquire properties and behaviors of another class. Polymorphism enables objects to take multiple forms.

Key concepts:
- `extends` keyword for inheritance
- `super` to access parent class members
- Method overriding with `@Override`
- `final` methods cannot be overridden
- `final` classes cannot be extended
- Polymorphism: parent reference can hold child object
- Dynamic method dispatch at runtime

## Example

```java
// Parent class
public class Animal {
    protected String name;
    
    public Animal(String name) {
        this.name = name;
    }
    
    public void makeSound() {
        System.out.println("Some sound");
    }
}

// Child class
public class Dog extends Animal {
    public Dog(String name) {
        super(name);
    }
    
    @Override
    public void makeSound() {
        System.out.println(name + " says Woof!");
    }
}

// Polymorphism
Animal myPet = new Dog("Buddy");
myPet.makeSound();  // "Buddy says Woof!" (runtime dispatch)
```

## Real World Example

Payment processing systems use a base `Payment` class with subclasses `CreditCardPayment`, `PayPalPayment`, `UPIPayment`. Each overrides a `processPayment()` method. The system calls `processPayment()` polymorphically without knowing the concrete type.

## Summary

Inheritance promotes code reuse through `extends`. Polymorphism allows flexible code that works with the parent type while executing child-specific behavior. Use `@Override` to provide specialized implementations.

## Practice

1. Create a `Vehicle` parent class with `move()` method
2. Create `Car` and `Bicycle` subclasses that override `move()`
3. Write a method that accepts `Vehicle` and calls `move()` polymorphically
4. Test with different vehicle types