package org.example.model;

public class InstructionInfo {
    private int opcode;
    private int index;
    private String instructionType; // "METHOD_CALL", "CONTROL_FLOW", "OTHER"
    private String targetMethod; // For method calls: class.method descriptor
    private int targetIndex; // For control flow: target instruction index

    public InstructionInfo(int opcode, int index) {
        this.opcode = opcode;
        this.index = index;
        this.instructionType = "OTHER";
    }

    // Getters and setters
    public int getOpcode() { return opcode; }
    public void setOpcode(int opcode) { this.opcode = opcode; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getInstructionType() { return instructionType; }
    public void setInstructionType(String instructionType) { this.instructionType = instructionType; }

    public String getTargetMethod() { return targetMethod; }
    public void setTargetMethod(String targetMethod) { this.targetMethod = targetMethod; }

    public int getTargetIndex() { return targetIndex; }
    public void setTargetIndex(int targetIndex) { this.targetIndex = targetIndex; }

    public boolean isMethodCall() {
        return "METHOD_CALL".equals(instructionType);
    }

    public boolean isControlFlow() {
        return "CONTROL_FLOW".equals(instructionType);
    }
}