import java.lang.reflect.Method; 
 
public class ReflectionTest { 
    public static void main(String[] args) throws Exception { 
        // Direct call 
        normalMethod(); 
 
        // Reflection call 
        Class<?> clazz = Class.forName("ReflectionTest"); 
        Method method = clazz.getMethod("reflectionMethod"); 
        method.invoke(null); 
    } 
 
    public static void normalMethod() { 
        System.out.println("Normal method via direct call"); 
    } 
 
    public static void reflectionMethod() { 
        System.out.println("Method called via reflection"); 
    } 
 
    public static void neverCalledReflection() { 
        System.out.println("This reflection method is never called"); 
    } 
} 
