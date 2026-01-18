package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private String ownerClass;
    private String methodName;
    private String descriptor;
    private int accessFlags;

    private List<InstructionInfo> instructions;
    private List<ReflectionCall> reflectionCalls;     // NEW: tracks reflection API usage
    private List<String> stringConstants;             // NEW: captures string literals (useful for reflection analysis)

    public MethodInfo(String ownerClass, String methodName, String descriptor, int accessFlags) {
        this.ownerClass = ownerClass;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.accessFlags = accessFlags;

        this.instructions = new ArrayList<>();
        this.reflectionCalls = new ArrayList<>();
        this.stringConstants = new ArrayList<>();
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public String getOwnerClass() {
        return ownerClass;
    }

    public void setOwnerClass(String ownerClass) {
        this.ownerClass = ownerClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(int accessFlags) {
        this.accessFlags = accessFlags;
    }

    // Instructions
    public List<InstructionInfo> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<InstructionInfo> instructions) {
        this.instructions = instructions;
    }

    public void addInstruction(InstructionInfo instruction) {
        this.instructions.add(instruction);
    }

    // Reflection calls
    public List<ReflectionCall> getReflectionCalls() {
        return reflectionCalls;
    }

    public void setReflectionCalls(List<ReflectionCall> reflectionCalls) {
        this.reflectionCalls = reflectionCalls;
    }

    public void addReflectionCall(ReflectionCall call) {
        this.reflectionCalls.add(call);
    }

    // String constants (LDC instructions)
    public List<String> getStringConstants() {
        return stringConstants;
    }

    public void setStringConstants(List<String> stringConstants) {
        this.stringConstants = stringConstants;
    }

    public void addStringConstant(String constant) {
        this.stringConstants.add(constant);
    }

    // ── Convenience / Utility Methods ──────────────────────────────────────────

    /**
     * Returns the full JVM-style method identifier (class.method(descriptor))
     */
    public String getFullName() {
        return ownerClass + "." + methodName + descriptor;
    }

    /**
     * Checks if the method is static (ACC_STATIC flag is set)
     */
    public boolean isStatic() {
        return (accessFlags & 0x0008) != 0; // 0x0008 = ACC_STATIC
    }

    /**
     * Quick check if this method contains any reflection calls
     */
    public boolean usesReflection() {
        return !reflectionCalls.isEmpty();
    }

    /**
     * Returns the number of reflection-related operations found in this method
     */
    public int getReflectionCallCount() {
        return reflectionCalls.size();
    }
}