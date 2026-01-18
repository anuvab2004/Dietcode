package org.example.report;

import org.example.model.FieldInfo;
import org.example.model.MethodInfo;

import java.util.*;


//Container class for all analysis results to be presented in reports.

public class Report {

    private List<MethodInfo> deadMethods;
    private Map<String, Set<Integer>> deadBlocks;
    private List<FieldInfo> deadFields;

    private int totalMethodsAnalyzed;
    private int totalDeadMethods;
    private int totalDeadBlocks;
    private int totalDeadFields;

    private int totalReflectionCalls;           // NEW: total number of reflection operations found

    public Report() {
        this.deadMethods = new ArrayList<>();
        this.deadBlocks = new HashMap<>();
        this.deadFields = new ArrayList<>();
        // totalReflectionCalls defaults to 0
    }

    // ── Dead Methods ───────────────────────────────────────────────────────────

    public List<MethodInfo> getDeadMethods() {
        return Collections.unmodifiableList(deadMethods);
    }

    public void setDeadMethods(List<MethodInfo> deadMethods) {
        this.deadMethods = new ArrayList<>(deadMethods);
    }

    public void addDeadMethod(MethodInfo method) {
        this.deadMethods.add(method);
    }

    // ── Dead Code Blocks ───────────────────────────────────────────────────────

    public Map<String, Set<Integer>> getDeadBlocks() {
        return Collections.unmodifiableMap(deadBlocks);
    }

    public void setDeadBlocks(Map<String, Set<Integer>> deadBlocks) {
        this.deadBlocks = new HashMap<>();
        deadBlocks.forEach((k, v) -> this.deadBlocks.put(k, new HashSet<>(v)));
    }

    public void addDeadBlocks(String methodKey, Set<Integer> blocks) {
        this.deadBlocks.put(methodKey, new HashSet<>(blocks));
    }

    // ── Dead Fields ────────────────────────────────────────────────────────────

    public List<FieldInfo> getDeadFields() {
        return Collections.unmodifiableList(deadFields);
    }

    public void setDeadFields(List<FieldInfo> deadFields) {
        this.deadFields = new ArrayList<>(deadFields);
    }

    public void addDeadField(FieldInfo field) {
        this.deadFields.add(field);
    }

    // ── Summary Counts ─────────────────────────────────────────────────────────

    public int getTotalMethodsAnalyzed() {
        return totalMethodsAnalyzed;
    }

    public void setTotalMethodsAnalyzed(int totalMethodsAnalyzed) {
        this.totalMethodsAnalyzed = totalMethodsAnalyzed;
    }

    public int getTotalDeadMethods() {
        return totalDeadMethods;
    }

    public void setTotalDeadMethods(int totalDeadMethods) {
        this.totalDeadMethods = totalDeadMethods;
    }

    public int getTotalDeadBlocks() {
        return totalDeadBlocks;
    }

    public void setTotalDeadBlocks(int totalDeadBlocks) {
        this.totalDeadBlocks = totalDeadBlocks;
    }

    public int getTotalDeadFields() {
        return totalDeadFields;
    }

    public void setTotalDeadFields(int totalDeadFields) {
        this.totalDeadFields = totalDeadFields;
    }

    // ── Reflection Statistics (NEW) ────────────────────────────────────────────

    /**
     * Returns the total number of reflection-related operations found across all methods
     * (Class.forName, Method.invoke, getMethod, getDeclaredMethod, etc.)
     */
    public int getTotalReflectionCalls() {
        return totalReflectionCalls;
    }

    public void setTotalReflectionCalls(int totalReflectionCalls) {
        this.totalReflectionCalls = totalReflectionCalls;
    }

    // ── Utility Methods ────────────────────────────────────────────────────────

    /**
     * Recalculates all total counts based on current collections.
     * Useful when collections were modified directly.
     */
    public void calculateTotals() {
        this.totalDeadMethods = deadMethods.size();
        this.totalDeadFields = deadFields != null ? deadFields.size() : 0;

        this.totalDeadBlocks = 0;
        for (Set<Integer> blocks : deadBlocks.values()) {
            this.totalDeadBlocks += blocks.size();
        }

        // Note: totalReflectionCalls is set externally (from ReflectionAnalyzer)
        // and is not recalculated here
    }


     //Quick check if the report contains any findings

    public boolean hasFindings() {
        return !deadMethods.isEmpty() ||
                !deadBlocks.isEmpty() ||
                !deadFields.isEmpty() ||
                totalReflectionCalls > 0;
    }


     //Returns a short summary string for logging/quick display

    @Override
    public String toString() {
        return String.format(
                "Analysis Report: %d methods analyzed, %d dead methods, %d dead blocks, " +
                        "%d dead fields, %d reflection calls",
                totalMethodsAnalyzed,
                totalDeadMethods,
                totalDeadBlocks,
                totalDeadFields,
                totalReflectionCalls
        );
    }
}