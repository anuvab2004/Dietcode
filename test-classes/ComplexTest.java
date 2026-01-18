public class ComplexTest { 
    public static void main(String[] args) { 
        System.out.println("Main method"); 
        methodA(); 
    } 
 
    public static void methodA() { 
        System.out.println("Method A"); 
        methodB(); 
    } 
 
    public static void methodB() { 
        System.out.println("Method B"); 
        // Conditional logic 
        if (false) { 
            deadBlock(); 
        } 
    } 
 
    public static void deadBlock() { 
        System.out.println("This block is never executed"); 
    } 
 
    public static void completelyDead() { 
        System.out.println("Completely dead method"); 
    } 
} 
