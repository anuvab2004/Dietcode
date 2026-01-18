package org.example;

import org.example.analysis.DeadBlockAnalyzer;
import org.example.analysis.DeadFieldAnalyzer;
import org.example.analysis.DeadMethodAnalyzer;
import org.example.analysis.ReflectionAnalyzer;
import org.example.bytecode.ClassScanner;
import org.example.bytecode.MethodScanner;
import org.example.graph.CallGraph;
import org.example.graph.GraphNode;
import org.example.model.ClassInfo;
import org.example.model.FieldInfo;
import org.example.model.MethodInfo;
import org.example.report.ConsoleReporter;
import org.example.report.Report;
import org.example.util.ClassPathScanner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String inputPath = args[0];

        try {
            System.out.println("Java Dead Code & Reflection Analyzer");
            System.out.println("=====================================");
            System.out.println("Analyzing: " + inputPath);
            System.out.println();

            // 1. Collect all class files
            ClassPathScanner pathScanner = new ClassPathScanner();
            List<byte[]> classBytesList = pathScanner.scanClassFiles(inputPath);

            if (classBytesList.isEmpty()) {
                System.out.println("No .class files found in: " + inputPath);
                return;
            }

            System.out.printf("Found %,d class files%n%n", classBytesList.size());

            // 2. Build initial call graph (static calls)
            CallGraph callGraph = buildCallGraph(classBytesList);
            System.out.printf("Initial call graph: %,d methods%n%n", callGraph.getAllNodes().size());

            // 3. Extract all classes info (needed for both method and field analysis)
            List<ClassInfo> allClasses = extractAllClassesWithFields(classBytesList);

            // 4. Reflection analysis & call graph enhancement
            System.out.println("=== Reflection Analysis ===");
            ReflectionAnalyzer reflectionAnalyzer = new ReflectionAnalyzer();

            List<MethodInfo> allMethods = extractAllMethods(callGraph);
            reflectionAnalyzer.analyzeReflection(allMethods, callGraph);

            printReflectionSummary(reflectionAnalyzer.getReflectionSummary(allMethods));

            System.out.printf("%nCall graph after reflection enhancement: %,d methods%n%n",
                    callGraph.getAllNodes().size());

            // 5. Dead method analysis + immediate reporting
            System.out.println("=== Dead Method Analysis ===");
            DeadMethodAnalyzer deadMethodAnalyzer = new DeadMethodAnalyzer();

            // Pass both callGraph and allClasses as required
            List<MethodInfo> deadMethods = deadMethodAnalyzer.findDeadMethods(callGraph, allClasses);

            // Print dead methods results right after detection
            deadMethodAnalyzer.printDeadMethods(deadMethods);

            // Also show categorized view
            Map<String, List<String>> categorizedDeadMethods =
                    deadMethodAnalyzer.categorizeDeadMethods(deadMethods);
            printCategorizedDeadMethods(categorizedDeadMethods);

            // 6. Dead field analysis
            System.out.println("\n=== Field Analysis ===");
            DeadFieldAnalyzer deadFieldAnalyzer = new DeadFieldAnalyzer();

            List<FieldInfo> deadFields = deadFieldAnalyzer.findDeadFields(allClasses);

            deadFieldAnalyzer.printFieldAnalysisReport(allClasses);
            printDeadFieldsReport(deadFields, deadFieldAnalyzer);

            // 7. Dead code block analysis (only in live methods)
            System.out.println("\n=== Dead Code Block Analysis ===");
            DeadBlockAnalyzer deadBlockAnalyzer = new DeadBlockAnalyzer();
            List<MethodInfo> liveMethods = getLiveMethods(callGraph.getAllNodes(), deadMethods);

            Map<String, Map<String, Set<Integer>>> deadBlocksByClass =
                    deadBlockAnalyzer.findDeadBlocksInMethods(liveMethods);

            // Print dead blocks summary
            int totalDeadBlocks = 0;
            for (Map<String, Set<Integer>> classBlocks : deadBlocksByClass.values()) {
                for (Set<Integer> blocks : classBlocks.values()) {
                    totalDeadBlocks += blocks.size();
                }
            }
            System.out.printf("Found %,d dead code blocks in live methods%n%n", totalDeadBlocks);

            // 8. Build final consolidated report
            Report report = createFinalReport(
                    callGraph,
                    deadMethods,
                    deadBlocksByClass,
                    deadFields,
                    reflectionAnalyzer.getReflectionSummary(allMethods)
            );

            // 9. Final formatted report output
            ConsoleReporter reporter = new ConsoleReporter();
            reporter.printReport(report);

        } catch (IOException e) {
            System.err.println("I/O error during analysis: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Unexpected error during analysis: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }

    /**
     * Performs complete dead code analysis and returns the result as a Report object.
     * This method produces no console output and is suitable for programmatic use.
     *
     * @param inputPath path to directory, JAR, or single .class file
     * @return analysis report
     * @throws IOException if class file reading fails
     */
    public static Report analyzeAndGetReport(String inputPath) throws IOException {
        Report report = new Report();

        ClassPathScanner pathScanner = new ClassPathScanner();
        List<byte[]> classBytesList = pathScanner.scanClassFiles(inputPath);

        if (classBytesList.isEmpty()) {
            report.setTotalMethodsAnalyzed(0);
            return report;
        }

        // Build call graph from static calls
        CallGraph callGraph = buildCallGraph(classBytesList);

        // Reflection analysis & graph enhancement
        ReflectionAnalyzer reflectionAnalyzer = new ReflectionAnalyzer();
        List<MethodInfo> allMethods = extractAllMethods(callGraph);
        reflectionAnalyzer.analyzeReflection(allMethods, callGraph);

        // Extract full class information (methods + fields)
        List<ClassInfo> allClasses = extractAllClassesWithFields(classBytesList);

        // Dead method analysis
        DeadMethodAnalyzer deadMethodAnalyzer = new DeadMethodAnalyzer();
        List<MethodInfo> deadMethods = deadMethodAnalyzer.findDeadMethods(callGraph, allClasses);

        // Dead field analysis
        DeadFieldAnalyzer deadFieldAnalyzer = new DeadFieldAnalyzer();
        List<FieldInfo> deadFields = deadFieldAnalyzer.findDeadFields(allClasses);

        // Dead block analysis (only in live methods)
        DeadBlockAnalyzer deadBlockAnalyzer = new DeadBlockAnalyzer();
        List<MethodInfo> liveMethods = getLiveMethods(callGraph.getAllNodes(), deadMethods);
        Map<String, Map<String, Set<Integer>>> deadBlocksByClass =
                deadBlockAnalyzer.findDeadBlocksInMethods(liveMethods);

        // Populate report
        report.setDeadMethods(deadMethods);
        report.setDeadFields(deadFields);

        report.setTotalMethodsAnalyzed(callGraph.getAllNodes().size());
        report.setTotalDeadMethods(deadMethods.size());
        report.setTotalDeadFields(deadFields.size());

        // Calculate total dead blocks
        int totalDeadBlocks = 0;
        for (Map<String, Set<Integer>> classBlocks : deadBlocksByClass.values()) {
            for (Set<Integer> blocks : classBlocks.values()) {
                totalDeadBlocks += blocks.size();
            }
        }
        report.setTotalDeadBlocks(totalDeadBlocks);

        // Optional: reflection statistics
        Map<String, List<String>> reflectionSummary = reflectionAnalyzer.getReflectionSummary(allMethods);
        int totalReflectionCalls = reflectionSummary.values().stream()
                .mapToInt(List::size)
                .sum();
        report.setTotalReflectionCalls(totalReflectionCalls);

        return report;
    }

    // ── Helper Methods ─────────────────────────────────────────────────────────

    private static CallGraph buildCallGraph(List<byte[]> classBytesList) {
        CallGraph callGraph = new CallGraph();
        Map<String, List<MethodInfo>> classToMethods = new HashMap<>();

        for (byte[] bytes : classBytesList) {
            ClassScanner classScanner = new ClassScanner();
            ClassInfo classInfo = classScanner.scanClass(bytes);

            MethodScanner methodScanner = new MethodScanner();
            List<MethodInfo> methods = methodScanner.scanMethods(bytes, classInfo.getClassName());

            classToMethods.put(classInfo.getClassName(), methods);

            for (MethodInfo method : methods) {
                callGraph.addMethod(method);
            }
        }

        for (List<MethodInfo> methods : classToMethods.values()) {
            for (MethodInfo method : methods) {
                String caller = method.getFullName();
                for (var instr : method.getInstructions()) {
                    if (instr.isMethodCall() && instr.getTargetMethod() != null) {
                        callGraph.addCall(caller, instr.getTargetMethod());
                    }
                }
            }
        }

        return callGraph;
    }

    private static List<MethodInfo> extractAllMethods(CallGraph callGraph) {
        return callGraph.getAllNodes().stream()
                .map(GraphNode::getMethodInfo)
                .toList();
    }

    private static List<ClassInfo> extractAllClassesWithFields(List<byte[]> classBytesList) {
        List<ClassInfo> classes = new ArrayList<>();

        for (byte[] bytes : classBytesList) {
            ClassScanner classScanner = new ClassScanner();
            ClassInfo classInfo = classScanner.scanClass(bytes);

            MethodScanner scanner = new MethodScanner();
            classInfo.setMethods(scanner.scanMethods(bytes, classInfo.getClassName()));
            classInfo.setFields(scanner.scanFields(bytes, classInfo.getClassName()));

            classes.add(classInfo);
        }

        return classes;
    }

    private static List<MethodInfo> getLiveMethods(Collection<GraphNode> nodes, List<MethodInfo> deadMethods) {
        Set<String> deadIds = deadMethods.stream()
                .map(MethodInfo::getFullName)
                .collect(Collectors.toSet());

        return nodes.stream()
                .map(GraphNode::getMethodInfo)
                .filter(m -> !deadIds.contains(m.getFullName()))
                .toList();
    }

    private static void printReflectionSummary(Map<String, List<String>> summary) {
        if (summary.isEmpty()) {
            System.out.println("No reflection usage detected.");
            return;
        }

        System.out.println("\nReflection Calls Found:");
        System.out.println("─".repeat(60));

        int total = 0;
        for (Map.Entry<String, List<String>> entry : summary.entrySet()) {
            System.out.printf(" %s (%d):%n", entry.getKey(), entry.getValue().size());
            for (String call : entry.getValue()) {
                System.out.println("   • " + call);
            }
            total += entry.getValue().size();
        }
        System.out.println("─".repeat(60));
        System.out.println("Total reflection operations found: " + total);
    }

    private static void printDeadFieldsReport(List<FieldInfo> deadFields, DeadFieldAnalyzer analyzer) {
        if (deadFields.isEmpty()) {
            System.out.println("No unused fields found.");
            return;
        }

        System.out.println("\nUNUSED / POTENTIALLY DEAD FIELDS:");
        System.out.println("─".repeat(70));

        Map<String, List<String>> categorized = analyzer.categorizeDeadFields(deadFields);

        for (Map.Entry<String, List<String>> entry : categorized.entrySet()) {
            System.out.println("Class: " + entry.getKey());
            for (String field : entry.getValue()) {
                System.out.println(" • " + field);
            }
            System.out.println();
        }
        System.out.println("Total potentially dead fields: " + deadFields.size());
    }

    private static void printCategorizedDeadMethods(Map<String, List<String>> categorized) {
        if (categorized.isEmpty()) {
            System.out.println("No dead methods found.");
            return;
        }

        System.out.println("\nCATEGORIZED DEAD METHODS:");
        System.out.println("─".repeat(70));

        for (Map.Entry<String, List<String>> entry : categorized.entrySet()) {
            System.out.println("Class: " + entry.getKey());
            for (String method : entry.getValue()) {
                System.out.println(" • " + method);
            }
            System.out.println();
        }
        System.out.println("─".repeat(70));
    }

    private static Report createFinalReport(
            CallGraph callGraph,
            List<MethodInfo> deadMethods,
            Map<String, Map<String, Set<Integer>>> deadBlocksByClass,
            List<FieldInfo> deadFields,
            Map<String, List<String>> reflectionSummary) {

        Report report = new Report();

        report.setDeadMethods(deadMethods);
        report.setDeadFields(deadFields);

        Map<String, Set<Integer>> flatDeadBlocks = new HashMap<>();
        deadBlocksByClass.values().forEach(flatDeadBlocks::putAll);
        report.setDeadBlocks(flatDeadBlocks);

        report.setTotalMethodsAnalyzed(callGraph.getAllNodes().size());
        report.setTotalDeadMethods(deadMethods.size());
        report.setTotalDeadFields(deadFields.size());

        int totalDeadBlocks = 0;
        for (Set<Integer> blocks : flatDeadBlocks.values()) {
            totalDeadBlocks += blocks.size();
        }
        report.setTotalDeadBlocks(totalDeadBlocks);

        int totalReflection = reflectionSummary.values().stream()
                .mapToInt(List::size)
                .sum();
        report.setTotalReflectionCalls(totalReflection);

        return report;
    }

    private static void printUsage() {
        System.out.println("Java Bytecode Dead Code & Reflection Analyzer");
        System.out.println("Usage: java -jar analyzer.jar <path>");
        System.out.println();
        System.out.println("  <path> can be:");
        System.out.println("    • directory with .class files");
        System.out.println("    • single .class file");
        System.out.println("    • .jar file");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar analyzer.jar ./target/classes");
        System.out.println("  java -jar analyzer.jar myapp.jar");
        System.out.println("  java -jar analyzer.jar com/example/MyClass.class");
    }
}