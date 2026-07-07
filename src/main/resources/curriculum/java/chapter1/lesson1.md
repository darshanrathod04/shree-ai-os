Title: Introduction to Java
Objective: Understand what Java is and set up your development environment.

## Explanation

Java is a high-level, object-oriented programming language developed by Sun Microsystems in 1995. It follows the "Write Once, Run Anywhere" philosophy through the Java Virtual Machine (JVM).

Key characteristics:
- Platform independent via JVM bytecode
- Object-oriented with classes and inheritance
- Strongly typed and statically compiled
- Automatic memory management via garbage collection
- Rich standard library (Java API)

## Example

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

This program defines a class `HelloWorld` with a `main` method. When executed, the JVM calls `main` and prints the message.

## Real World Example

Java powers Android apps (over 3 billion devices), enterprise banking systems (JPMorgan Chase uses Java for trading platforms), and big data frameworks like Apache Hadoop and Apache Spark.

## Summary

Java is a mature, platform-independent language with automatic memory management, a vast ecosystem, and strong typing. It remains one of the most widely used programming languages in enterprise and Android development.

## Practice

1. Install JDK 21 from Oracle or OpenJDK
2. Write a program that prints your name
3. Compile and run it using `javac` and `java`
4. Modify the program to print your age as well