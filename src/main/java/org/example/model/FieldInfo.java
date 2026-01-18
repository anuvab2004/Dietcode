package org.example.model;

public class FieldInfo {
    private String ownerClass;
    private String fieldName;
    private String descriptor;
    private int accessFlags;
    private boolean isRead;
    private boolean isWritten;

    public FieldInfo(String ownerClass, String fieldName, String descriptor, int accessFlags) {
        this.ownerClass = ownerClass;
        this.fieldName = fieldName;
        this.descriptor = descriptor;
        this.accessFlags = accessFlags;
        this.isRead = false;
        this.isWritten = false;
    }

    // Getters and setters
    public String getOwnerClass() { return ownerClass; }
    public String getFieldName() { return fieldName; }
    public String getDescriptor() { return descriptor; }
    public int getAccessFlags() { return accessFlags; }
    public boolean isRead() { return isRead; }
    public boolean isWritten() { return isWritten; }
    public void setRead(boolean read) { isRead = read; }
    public void setWritten(boolean written) { isWritten = written; }

    public String getFullName() {
        return ownerClass + "." + fieldName + ":" + descriptor;
    }

    public boolean isStatic() {
        return (accessFlags & 0x0008) != 0; // ACC_STATIC
    }

    public boolean isFinal() {
        return (accessFlags & 0x0010) != 0; // ACC_FINAL
    }

    public boolean isUsed() {
        return isRead || isWritten;
    }
}