package org.example.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassPathScanner {

    public List<byte[]> scanClassFiles(String inputPath) throws IOException {
        List<byte[]> classBytesList = new ArrayList<>();

        File input = new File(inputPath);
        if (!input.exists()) {
            throw new FileNotFoundException("Input path not found: " + inputPath);
        }

        if (input.isDirectory()) {
            scanDirectory(input, classBytesList);
        } else if (input.getName().endsWith(".jar")) {
            scanJarFile(input, classBytesList);
        } else if (input.getName().endsWith(".class")) {
            classBytesList.add(Files.readAllBytes(input.toPath()));
        }

        return classBytesList;
    }

    private void scanDirectory(File directory, List<byte[]> classBytesList) throws IOException {
        Files.walk(directory.toPath())
                .filter(path -> path.toString().endsWith(".class"))
                .forEach(path -> {
                    try {
                        classBytesList.add(Files.readAllBytes(path));
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + path + " - " + e.getMessage());
                    }
                });
    }

    private void scanJarFile(File jarFile, List<byte[]> classBytesList) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        byte[] bytes = readAllBytes(is);
                        classBytesList.add(bytes);
                    }
                }
            }
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    public List<String> getClassFilePaths(String inputPath) throws IOException {
        List<String> classPaths = new ArrayList<>();

        File input = new File(inputPath);
        if (!input.exists()) {
            throw new FileNotFoundException("Input path not found: " + inputPath);
        }

        if (input.isDirectory()) {
            Files.walk(input.toPath())
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> classPaths.add(path.toString()));
        } else if (input.getName().endsWith(".jar")) {
            classPaths.add(input.getAbsolutePath());
        } else if (input.getName().endsWith(".class")) {
            classPaths.add(input.getAbsolutePath());
        }

        return classPaths;
    }
}