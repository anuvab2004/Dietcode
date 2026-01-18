package org.example.bytecode;

import org.example.model.MethodInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InstructionScanner {

    public void scanInstructions(byte[] classBytes, MethodInfo methodInfo) {
        ClassReader classReader = new ClassReader(classBytes);

        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                // Only process the specific method we're interested in
                if (name.equals(methodInfo.getMethodName()) &&
                        descriptor.equals(methodInfo.getDescriptor())) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        private int instructionIndex = 0;

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name,
                                                    String descriptor, boolean isInterface) {
                            org.example.model.InstructionInfo instruction =
                                    new org.example.model.InstructionInfo(opcode, instructionIndex++);
                            instruction.setInstructionType("METHOD_CALL");
                            instruction.setTargetMethod(owner.replace("/", ".") + "." + name + descriptor);
                            methodInfo.addInstruction(instruction);
                        }

                        @Override
                        public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
                            org.example.model.InstructionInfo instruction =
                                    new org.example.model.InstructionInfo(opcode, instructionIndex++);
                            instruction.setInstructionType("CONTROL_FLOW");
                            methodInfo.addInstruction(instruction);
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            org.example.model.InstructionInfo instruction =
                                    new org.example.model.InstructionInfo(opcode, instructionIndex++);
                            methodInfo.addInstruction(instruction);
                        }

                        @Override
                        public void visitVarInsn(int opcode, int var) {
                            org.example.model.InstructionInfo instruction =
                                    new org.example.model.InstructionInfo(opcode, instructionIndex++);
                            methodInfo.addInstruction(instruction);
                        }
                    };
                }
                return null;
            }
        }, ClassReader.EXPAND_FRAMES);
    }
}