package org.example.analysis;

import org.example.model.ClassInfo;
import org.example.model.FieldInfo;
import java.util.*;

public class DeadFieldAnalyzer {
    private final FieldUsageAnalyzer fieldUsageAnalyzer;

    public DeadFieldAnalyzer() {
        this.fieldUsageAnalyzer = new FieldUsageAnalyzer();
    }

    public List<FieldInfo> findDeadFields(List<ClassInfo> classes) {
        Map<String, List<FieldInfo>> unusedFieldsMap =
                fieldUsageAnalyzer.findUnusedFields(classes);

        Map<String, List<FieldInfo>> writeOnlyFieldsMap =
                findWriteOnlyFields(classes);

        List<FieldInfo> allDeadFields = new ArrayList<>();

        for (List<FieldInfo> fields : unusedFieldsMap.values()) {
            allDeadFields.addAll(fields);
        }

        for (List<FieldInfo> fields : writeOnlyFieldsMap.values()) {
            for (FieldInfo field : fields) {
                if (!allDeadFields.contains(field)) {
                    allDeadFields.add(field);
                }
            }
        }

        return allDeadFields;
    }

    private Map<String, List<FieldInfo>> findWriteOnlyFields(List<ClassInfo> classes) {
        Map<String, List<FieldInfo>> writeOnlyFieldsByClass = new HashMap<>();

        for (ClassInfo classInfo : classes) {
            List<FieldInfo> writeOnlyFields = new ArrayList<>();

            for (FieldInfo field : classInfo.getFields()) {
                if (field.isWritten() && !field.isRead() && !shouldExcludeField(field)) {
                    writeOnlyFields.add(field);
                }
            }

            if (!writeOnlyFields.isEmpty()) {
                writeOnlyFieldsByClass.put(classInfo.getClassName(), writeOnlyFields);
            }
        }

        return writeOnlyFieldsByClass;
    }

    private boolean shouldExcludeField(FieldInfo field) {
        return field.isFinal() && field.isStatic() ||
                field.getFieldName().startsWith("$") ||
                field.getFieldName().equals("serialVersionUID");
    }

    public Map<String, List<String>> categorizeDeadFields(List<FieldInfo> deadFields) {
        Map<String, List<String>> categorized = new HashMap<>();

        for (FieldInfo field : deadFields) {
            String className = field.getOwnerClass();
            String fieldSignature = formatFieldSignature(field);

            categorized.putIfAbsent(className, new ArrayList<>());
            categorized.get(className).add(fieldSignature);
        }

        return categorized;
    }

    private String formatFieldSignature(FieldInfo field) {
        StringBuilder sb = new StringBuilder();

        if ((field.getAccessFlags() & 0x0001) != 0) sb.append("public ");
        else if ((field.getAccessFlags() & 0x0004) != 0) sb.append("protected ");
        else if ((field.getAccessFlags() & 0x0002) != 0) sb.append("private ");

        if ((field.getAccessFlags() & 0x0008) != 0) sb.append("static ");
        if ((field.getAccessFlags() & 0x0010) != 0) sb.append("final ");
        if ((field.getAccessFlags() & 0x0040) != 0) sb.append("volatile ");
        if ((field.getAccessFlags() & 0x0080) != 0) sb.append("transient ");

        sb.append(field.getDescriptor()).append(" ");
        sb.append(field.getFieldName());

        if (field.isWritten() && !field.isRead()) {
            sb.append(" [WRITE-ONLY]");
        }

        return sb.toString();
    }

    public void printFieldAnalysisReport(List<ClassInfo> classes) {
        Map<String, Map<String, Integer>> stats =
                fieldUsageAnalyzer.getFieldUsageStatistics(classes);

        System.out.println("\nFIELD USAGE ANALYSIS");
        System.out.println("=".repeat(50));

        for (Map.Entry<String, Map<String, Integer>> entry : stats.entrySet()) {
            String className = entry.getKey();
            Map<String, Integer> classStats = entry.getValue();

            System.out.printf("\nClass: %s%n", className);
            System.out.printf("  Total fields: %d%n", classStats.get("total"));
            System.out.printf("  Unused fields: %d%n", classStats.get("unused"));
            System.out.printf("  Read-only fields: %d%n", classStats.get("readOnly"));
            System.out.printf("  Write-only fields: %d%n", classStats.get("writeOnly"));
        }
    }
}