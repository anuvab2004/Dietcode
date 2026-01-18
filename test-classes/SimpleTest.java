public class SimpleTest { 
    public static void main(String[] args) { 
        liveMethod(); 
    } 
 
    public static void liveMethod() { 
        System.out.println("Live method called"); 
    } 
 
    public static void deadMethod() { 
        System.out.println("This method is never called"); 
    } 
 
    private void privateDeadMethod() { 
        System.out.println("Private dead method"); 
    } 
} 
