package org.example;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;

public class TestASM {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing ASM...");

        File classFile = new File("test-samples/SimpleTest.class");
        if (!classFile.exists()) {
            System.out.println("Class file not found: " + classFile.getAbsolutePath());
            return;
        }

        FileInputStream fis = new FileInputStream(classFile);
        ClassReader cr = new ClassReader(fis);

        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name,
                              String signature, String superName, String[] interfaces) {
                System.out.println("Class: " + name);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name,
                                             String descriptor, String signature, String[] exceptions) {
                System.out.println("  Method: " + name + descriptor);
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);

        System.out.println("ASM test completed!");
    }
}