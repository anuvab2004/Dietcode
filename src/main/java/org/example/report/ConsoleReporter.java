package org.example.report;

import org.example.analysis.DeadFieldAnalyzer;
import org.example.model.MethodInfo;
import org.example.model.FieldInfo;
import java.util.*;

public class ConsoleReporter {

    public void printReport(Report report) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEAD CODE ANALYSIS REPORT");
        System.out.println("=".repeat(80));

        printSummary(report);
        printDeadMethods(report);
        printDeadFields(report);
        printDeadBlocks(report);

        System.out.println("=".repeat(80));
        System.out.println("END OF REPORT");
        System.out.println("=".repeat(80));
    }

    private void printSummary(Report report) {
        System.out.println("\nSUMMARY:");
        System.out.println("-".repeat(40));
        System.out.printf("Total Methods Analyzed: %d%n", report.getTotalMethodsAnalyzed());
        System.out.printf("Total Dead Methods: %d%n", report.getTotalDeadMethods());
        System.out.printf("Total Dead Fields: %d%n", report.getTotalDeadFields());
        System.out.printf("Total Dead Blocks: %d%n", report.getTotalDeadBlocks());

        if (report.getTotalMethodsAnalyzed() > 0) {
            double deadMethodPercentage = (double) report.getTotalDeadMethods() / report.getTotalMethodsAnalyzed() * 100;
            System.out.printf("Dead Method Percentage: %.2f%%%n", deadMethodPercentage);
        }
    }

    private void printDeadMethods(Report report) {
        if (!report.getDeadMethods().isEmpty()) {
            System.out.println("\nDEAD METHODS:");
            System.out.println("-".repeat(40));

            Map<String, List<String>> categorized = categorizeDeadMethods(report.getDeadMethods());

            for (Map.Entry<String, List<String>> entry : categorized.entrySet()) {
                System.out.println("\nClass: " + entry.getKey());
                for (String method : entry.getValue()) {
                    System.out.println("  • " + method);
                }
            }
        } else {
            System.out.println("\nNo dead methods found!");
        }
    }

    private void printDeadFields(Report report) {
        if (report.getDeadFields() != null && !report.getDeadFields().isEmpty()) {
            System.out.println("\nUNUSED FIELDS:");
            System.out.println("-".repeat(40));

            DeadFieldAnalyzer deadFieldAnalyzer = new DeadFieldAnalyzer();
            Map<String, List<String>> categorized = deadFieldAnalyzer.categorizeDeadFields(report.getDeadFields());

            for (Map.Entry<String, List<String>> entry : categorized.entrySet()) {
                System.out.println("\nClass: " + entry.getKey());
                for (String field : entry.getValue()) {
                    System.out.println("  • " + field);
                }
            }
        } else {
            System.out.println("\nNo unused fields found!");
        }
    }

    private void printDeadBlocks(Report report) {
        if (!report.getDeadBlocks().isEmpty()) {
            System.out.println("\nDEAD CODE BLOCKS:");
            System.out.println("-".repeat(40));

            for (Map.Entry<String, Set<Integer>> entry : report.getDeadBlocks().entrySet()) {
                System.out.println("\nMethod: " + entry.getKey());
                System.out.println("  Unreachable instructions at indices: " + entry.getValue());
            }
        } else {
            System.out.println("\nNo dead code blocks found!");
        }
    }

    private Map<String, List<String>> categorizeDeadMethods(List<MethodInfo> deadMethods) {
        Map<String, List<String>> categorized = new HashMap<>();

        for (MethodInfo method : deadMethods) {
            String className = method.getOwnerClass();
            String methodSignature = method.getMethodName() + method.getDescriptor();

            categorized.putIfAbsent(className, new ArrayList<>());
            categorized.get(className).add(methodSignature);
        }

        return categorized;
    }

    private Map<String, List<String>> categorizeDeadFields(List<FieldInfo> deadFields) {
        Map<String, List<String>> categorized = new HashMap<>();

        for (FieldInfo field : deadFields) {
            String className = field.getOwnerClass();
            String fieldSignature = field.getFieldName() + ":" + field.getDescriptor();

            categorized.putIfAbsent(className, new ArrayList<>());
            categorized.get(className).add(fieldSignature);
        }

        return categorized;
    }

    public void printSimpleReport(List<MethodInfo> deadMethods) {
        System.out.println("\nDead Methods Found:");
        System.out.println("-".repeat(40));

        if (deadMethods.isEmpty()) {
            System.out.println("No dead methods found!");
        } else {
            for (MethodInfo method : deadMethods) {
                System.out.printf("%s.%s%s%n",
                        method.getOwnerClass(),
                        method.getMethodName(),
                        method.getDescriptor());
            }
        }
    }
}