import java.lang.reflect.Method;

public class ReflectionTestSample {

    public static void main(String[] args) throws Exception {
        // Static call (detected by normal analysis)
        StaticClass.staticMethod();

        // Reflection calls (detected by reflection analysis)
        Class<?> clazz = Class.forName("StaticClass");
        Method method = clazz.getMethod("reflectionMethod");
        method.invoke(null);

        // Dynamic method name
        String methodName = "dynamicMethod";
        Method dynamicMethod = clazz.getMethod(methodName);
        dynamicMethod.invoke(null);
    }
}

class StaticClass {
    public static void staticMethod() {
        System.out.println("Static method");
    }

    public static void reflectionMethod() {
        System.out.println("Called via reflection");
    }

    public static void dynamicMethod() {
        System.out.println("Dynamic method");
    }

    // This might be called via reflection
    public static void potentiallyUsed() {
        System.out.println("Maybe called");
    }

    // Definitely dead
    public static void definitelyDead() {
        System.out.println("Dead");
    }
}