package org.example.util;

public class AccessFlagUtils {

    public static boolean isPublic(int accessFlags) {
        return (accessFlags & 0x0001) != 0;
    }

    public static boolean isPrivate(int accessFlags) {
        return (accessFlags & 0x0002) != 0;
    }

    public static boolean isProtected(int accessFlags) {
        return (accessFlags & 0x0004) != 0;
    }

    public static boolean isStatic(int accessFlags) {
        return (accessFlags & 0x0008) != 0;
    }

    public static boolean isFinal(int accessFlags) {
        return (accessFlags & 0x0010) != 0;
    }

    public static boolean isSynchronized(int accessFlags) {
        return (accessFlags & 0x0020) != 0;
    }

    public static boolean isAbstract(int accessFlags) {
        return (accessFlags & 0x0400) != 0;
    }

    public static String getAccessModifierString(int accessFlags) {
        if (isPublic(accessFlags)) return "public";
        if (isProtected(accessFlags)) return "protected";
        if (isPrivate(accessFlags)) return "private";
        return "package-private";
    }

    public static String getAccessFlagsString(int accessFlags) {
        StringBuilder sb = new StringBuilder();

        if (isPublic(accessFlags)) sb.append("public ");
        if (isPrivate(accessFlags)) sb.append("private ");
        if (isProtected(accessFlags)) sb.append("protected ");
        if (isStatic(accessFlags)) sb.append("static ");
        if (isFinal(accessFlags)) sb.append("final ");
        if (isSynchronized(accessFlags)) sb.append("synchronized ");
        if (isAbstract(accessFlags)) sb.append("abstract ");

        return sb.toString().trim();
    }
}