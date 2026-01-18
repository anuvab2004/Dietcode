package org.example.bytecode;

import org.example.model.ClassInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;

public class ClassScanner {

    public ClassInfo scanClass(InputStream classStream) throws IOException {
        ClassReader classReader = new ClassReader(classStream);
        ClassInfo classInfo = new ClassInfo(classReader.getClassName().replace("/", "."));

        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                classInfo.setAccessFlags(access);
                super.visit(version, access, name, signature, superName, interfaces);
            }
        };

        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classInfo;
    }

    public ClassInfo scanClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassInfo classInfo = new ClassInfo(classReader.getClassName().replace("/", "."));

        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                classInfo.setAccessFlags(access);
                super.visit(version, access, name, signature, superName, interfaces);
            }
        };

        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        return classInfo;
    }
}