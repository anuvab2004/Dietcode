public class FieldTestSample {
    public static final String USED_CONSTANT = "Hello";
    private int usedInstanceField = 10;
    public static int usedStaticField = 20;

    private String unusedString = "Never used";
    public static final int UNUSED_CONSTANT = 100;
    protected double unusedDouble = 3.14;

    public void useFields() {
        System.out.println(USED_CONSTANT);
        usedInstanceField = 5;
        System.out.println(usedStaticField);

        unusedString = "Changed but never read";
    }

    public int getUsedInstanceField() {
        return usedInstanceField;
    }

    private int writeOnlyField;

    public void setWriteOnlyField(int value) {
        writeOnlyField = value;
    }
}