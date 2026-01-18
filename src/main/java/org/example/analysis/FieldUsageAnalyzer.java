package org.example.analysis;

import org.example.model.ClassInfo;
import org.example.model.FieldInfo;
import org.example.model.InstructionInfo;
import org.example.model.MethodInfo;
import java.util.*;

public class FieldUsageAnalyzer {

    public Map<String, List<FieldInfo>> findUnusedFields(List<ClassInfo> classes) {
        Map<String, List<FieldInfo>> unusedFieldsByClass = new HashMap<>();

        // First pass: Collect all fields
        Map<String, FieldInfo> allFields = new HashMap<>();
        for (ClassInfo classInfo : classes) {
            for (FieldInfo field : classInfo.getFields()) {
                allFields.put(field.getFullName(), field);
            }
        }

        // Second pass: Analyze field usage in methods
        for (ClassInfo classInfo : classes) {
            for (MethodInfo method : classInfo.getMethods()) {
                analyzeFieldUsageInMethod(method, allFields);
            }
        }

        // Third pass: Identify unused fields
        for (ClassInfo classInfo : classes) {
            List<FieldInfo> unusedFields = new ArrayList<>();
            for (FieldInfo field : classInfo.getFields()) {
                if (!field.isUsed() && !shouldExcludeField(field)) {
                    unusedFields.add(field);
                }
            }
            if (!unusedFields.isEmpty()) {
                unusedFieldsByClass.put(classInfo.getClassName(), unusedFields);
            }
        }

        return unusedFieldsByClass;
    }

    private void analyzeFieldUsageInMethod(MethodInfo method, Map<String, FieldInfo> allFields) {
        for (InstructionInfo instr : method.getInstructions()) {
            if (instr.getTargetMethod() != null && instr.getTargetMethod().contains(":")) {
                String fieldSignature = instr.getTargetMethod();
                FieldInfo field = allFields.get(fieldSignature);

                if (field != null) {
                    if ("FIELD_READ".equals(instr.getInstructionType())) {
                        field.setRead(true);
                    } else if ("FIELD_WRITE".equals(instr.getInstructionType())) {
                        field.setWritten(true);
                    }
                }
            }
        }
    }

    private boolean shouldExcludeField(FieldInfo field) {
        // Exclude fields that shouldn't be flagged as unused
        return field.isFinal() && field.isStatic() ||  // Constants
                field.getFieldName().startsWith("$") || // Synthetic fields
                field.getFieldName().equals("serialVersionUID"); // Serialization
    }

    public Map<String, Map<String, Integer>> getFieldUsageStatistics(List<ClassInfo> classes) {
        Map<String, Map<String, Integer>> stats = new HashMap<>();

        for (ClassInfo classInfo : classes) {
            int totalFields = 0;
            int unusedFields = 0;
            int readOnlyFields = 0;
            int writeOnlyFields = 0;

            for (FieldInfo field : classInfo.getFields()) {
                totalFields++;
                if (!field.isUsed()) {
                    unusedFields++;
                } else if (field.isRead() && !field.isWritten()) {
                    readOnlyFields++;
                } else if (!field.isRead() && field.isWritten()) {
                    writeOnlyFields++;
                }
            }

            Map<String, Integer> classStats = new HashMap<>();
            classStats.put("total", totalFields);
            classStats.put("unused", unusedFields);
            classStats.put("readOnly", readOnlyFields);
            classStats.put("writeOnly", writeOnlyFields);

            stats.put(classInfo.getClassName(), classStats);
        }

        return stats;
    }
}