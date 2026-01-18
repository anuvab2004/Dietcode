package org.example;

public class TestMain {
    public static void main(String[] args) {
        System.out.println("Hello from TestMain!");
        System.out.println("Arguments received: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("  arg[" + i + "] = " + args[i]);
        }
    }
}