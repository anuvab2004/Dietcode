package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private String className;
    private List<MethodInfo> methods;
    private List<FieldInfo> fields;
    private int accessFlags;

    public ClassInfo(String className) {
        this.className = className;
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    // Getters and setters
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public List<MethodInfo> getMethods() { return methods; }
    public void setMethods(List<MethodInfo> methods) { this.methods = methods; }
    public void addMethod(MethodInfo method) { this.methods.add(method); }

    public List<FieldInfo> getFields() { return fields; }
    public void setFields(List<FieldInfo> fields) { this.fields = fields; }
    public void addField(FieldInfo field) { this.fields.add(field); }

    public int getAccessFlags() { return accessFlags; }
    public void setAccessFlags(int accessFlags) { this.accessFlags = accessFlags; }
}