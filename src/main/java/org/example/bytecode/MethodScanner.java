package org.example.bytecode;

import org.example.model.ClassInfo;
import org.example.model.MethodInfo;
import org.example.model.InstructionInfo;
import org.example.model.FieldInfo;
import org.example.model.ReflectionCall;  // Assuming this model class exists
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class MethodScanner {

    public List<MethodInfo> scanMethods(byte[] classBytes, String className) {
        List<MethodInfo> methods = new ArrayList<>();
        ClassReader classReader = new ClassReader(classBytes);

        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodInfo methodInfo = new MethodInfo(
                        className,
                        name,
                        descriptor,
                        access
                );
                methods.add(methodInfo);

                // Return a MethodVisitor to scan instructions
                return new InstructionScanner(methodInfo);
            }
        }, ClassReader.EXPAND_FRAMES);

        return methods;
    }

    public List<FieldInfo> scanFields(byte[] classBytes, String className) {
        List<FieldInfo> fields = new ArrayList<>();
        ClassReader classReader = new ClassReader(classBytes);

        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor,
                                           String signature, Object value) {
                FieldInfo fieldInfo = new FieldInfo(
                        className, name, descriptor, access
                );
                fields.add(fieldInfo);
                return super.visitField(access, name, descriptor, signature, value);
            }
        }, ClassReader.EXPAND_FRAMES);

        return fields;
    }

    // Inner class for scanning instructions within a method
    private static class InstructionScanner extends MethodVisitor {
        private final MethodInfo methodInfo;
        private int instructionIndex = 0;

        public InstructionScanner(MethodInfo methodInfo) {
            super(Opcodes.ASM9);
            this.methodInfo = methodInfo;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
            // First, detect reflection API calls
            if (owner.equals("java/lang/Class")) {
                if (name.equals("forName")) {
                    trackReflectionCall("CLASS_FOR_NAME", opcode, owner, name, descriptor);
                    return; // We handled it as reflection
                } else if (name.equals("getMethod") || name.equals("getDeclaredMethod")) {
                    trackReflectionCall("GET_METHOD", opcode, owner, name, descriptor);
                    return;
                }
            } else if (owner.equals("java/lang/reflect/Method")) {
                if (name.equals("invoke")) {
                    trackReflectionCall("METHOD_INVOKE", opcode, owner, name, descriptor);
                    return;
                }
            }

            // Normal method call tracking (non-reflection)
            InstructionInfo instruction = new InstructionInfo(opcode, instructionIndex++);
            instruction.setInstructionType("METHOD_CALL");
            instruction.setTargetMethod(owner.replace("/", ".") + "." + name + descriptor);
            methodInfo.addInstruction(instruction);
        }

        private void trackReflectionCall(String type, int opcode, String owner, String name, String descriptor) {
            InstructionInfo instruction = new InstructionInfo(opcode, instructionIndex++);
            instruction.setInstructionType("REFLECTION_CALL");
            instruction.setTargetMethod(owner.replace("/", ".") + "." + name + descriptor);
            methodInfo.addInstruction(instruction);

            // Also store as structured reflection metadata
            ReflectionCall reflectionCall = new ReflectionCall(
                    owner.replace("/", "."),
                    name,
                    descriptor,
                    type
            );
            methodInfo.addReflectionCall(reflectionCall);
        }

        @Override
        public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
            InstructionInfo instruction = new InstructionInfo(opcode, instructionIndex++);
            instruction.setInstructionType("CONTROL_FLOW");
            methodInfo.addInstruction(instruction);
        }

        @Override
        public void visitInsn(int opcode) {
            InstructionInfo instruction = new InstructionInfo(opcode, instructionIndex++);
            methodInfo.addInstruction(instruction);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            InstructionInfo instruction = new InstructionInfo(opcode, instructionIndex++);
            methodInfo.addInstruction(instruction);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            InstructionInfo instruction = new InstructionInfo(opcode, instructionIndex++);

            // Track field operations
            if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
                instruction.setInstructionType("FIELD_READ");
            } else if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
                instruction.setInstructionType("FIELD_WRITE");
            } else {
                instruction.setInstructionType("FIELD_ACCESS");
            }

            instruction.setTargetMethod(owner.replace("/", ".") + "." + name + ":" + descriptor);
            methodInfo.addInstruction(instruction);
        }

        // Capture string constants (useful for reflection class/method names)
        @Override
        public void visitLdcInsn(Object value) {
            if (value instanceof String) {
                String stringConst = (String) value;
                methodInfo.addStringConstant(stringConst);
            }
            super.visitLdcInsn(value);
        }
    }
}