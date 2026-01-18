public class FieldTest { 
    // Used field 
    private static int usedField = 100; 
 
    // Unused field 
    private static String unusedField = "Never accessed"; 
 
    // Field used only in dead method 
    private static int fieldInDeadCode = 200; 
 
    public static void main(String[] args) { 
        System.out.println("Used field value: " + usedField); 
    } 
 
    public static void deadMethod() { 
        System.out.println("Dead method accessing field: " + fieldInDeadCode); 
    } 
} 
