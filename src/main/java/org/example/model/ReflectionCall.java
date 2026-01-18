package org.example.model;

public class ReflectionCall {
    private String className;
    private String methodName;
    private String descriptor;
    private int lineNumber;
    private String reflectionType; // GET_METHOD, INVOKE, FOR_NAME

    public ReflectionCall(String className, String methodName, String descriptor,
                          String reflectionType) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.reflectionType = reflectionType;
    }

    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public String getDescriptor() { return descriptor; }
    public int getLineNumber() { return lineNumber; }
    public String getReflectionType() { return reflectionType; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public String getTargetSignature() {
        return className + "." + methodName + descriptor;
    }

    @Override
    public String toString() {
        return reflectionType + ": " + getTargetSignature();
    }
}