package org.example.analysis;

import org.example.graph.CallGraph;
import org.example.graph.GraphNode;
import org.example.model.MethodInfo;

import java.util.*;

/**
 * Resolves potential entry points in a Java application that should be considered
 * reachable even if they have no incoming calls in the static call graph.
 */
public class EntryPointResolver {

    // Cache for classes that contain a main method (used by shouldExcludeFromDead)
    private final Set<String> classesWithMainMethod = new HashSet<>();

    /**
     * Identifies all reachable entry points in the given call graph.
     */
    public Set<GraphNode> resolveEntryPoints(CallGraph callGraph) {
        Set<GraphNode> entryPoints = new HashSet<>();

        // Standard Java main methods
        findMainMethods(callGraph, entryPoints);

        // JUnit test methods
        findJUnitTestMethods(callGraph, entryPoints);

        // Public static utility methods with no callers
        findPublicStaticMethods(callGraph, entryPoints);

        // Constructors of classes that have main methods
        findMainClassConstructors(callGraph, entryPoints);

        return entryPoints;
    }

    private void findMainMethods(CallGraph callGraph, Set<GraphNode> entryPoints) {
        for (GraphNode node : callGraph.getAllNodes()) {
            MethodInfo method = node.getMethodInfo();
            if (isMainMethod(method)) {
                entryPoints.add(node);
                classesWithMainMethod.add(method.getOwnerClass()); // Cache for later use
                System.out.println("[Entry Point] Main method: " + node.getNodeId());
            }
        }
    }

    private void findJUnitTestMethods(CallGraph callGraph, Set<GraphNode> entryPoints) {
        for (GraphNode node : callGraph.getAllNodes()) {
            MethodInfo method = node.getMethodInfo();
            if (isTestMethod(method)) {
                entryPoints.add(node);
                System.out.println("[Entry Point] Test method: " + node.getNodeId());
            }
        }
    }

    private void findPublicStaticMethods(CallGraph callGraph, Set<GraphNode> entryPoints) {
        for (GraphNode node : callGraph.getAllNodes()) {
            MethodInfo method = node.getMethodInfo();
            if (isPublicStaticUtility(method) && node.getIncomingCalls().isEmpty()) {
                entryPoints.add(node);
                System.out.println("[Entry Point] Public static utility: " + node.getNodeId());
            }
        }
    }

    private void findMainClassConstructors(CallGraph callGraph, Set<GraphNode> entryPoints) {
        for (GraphNode node : callGraph.getAllNodes()) {
            MethodInfo method = node.getMethodInfo();
            if (method.getMethodName().equals("<init>") &&
                    classesWithMainMethod.contains(method.getOwnerClass())) {
                entryPoints.add(node);
                System.out.println("[Entry Point] Constructor of main class: " + node.getNodeId());
            }
        }
    }

    /**
     * Determines whether a method should be excluded from dead code detection,
     * even if it appears unreachable in the call graph.
     */
    public boolean shouldExcludeFromDead(MethodInfo method) {
        String methodName = method.getMethodName();

        // Static initializers are always executed when the class is loaded
        if (methodName.equals("<clinit>")) {
            return true;
        }

        // Constructors of classes that contain a main method
        if (methodName.equals("<init>") && classesWithMainMethod.contains(method.getOwnerClass())) {
            return true;
        }

        return false;
    }

    // ── Helper methods ─────────────────────────────────────────────────────────

    private boolean isMainMethod(MethodInfo method) {
        return "main".equals(method.getMethodName()) &&
                "([Ljava/lang/String;)V".equals(method.getDescriptor()) &&
                method.isStatic() &&
                (method.getAccessFlags() & 0x0001) != 0; // ACC_PUBLIC
    }

    private boolean isTestMethod(MethodInfo method) {
        String name = method.getMethodName();
        return name.startsWith("test") ||
                name.endsWith("Test") ||
                name.equals("setUp") ||
                name.equals("tearDown") ||
                name.equals("before") ||
                name.equals("after");
    }

    private boolean isPublicStaticUtility(MethodInfo method) {
        return method.isStatic() &&
                (method.getAccessFlags() & 0x0001) != 0 && // ACC_PUBLIC
                !method.getMethodName().equals("<init>") &&
                !method.getMethodName().equals("<clinit>") &&
                !method.getMethodName().startsWith("lambda$") &&
                !method.getMethodName().contains("$");
    }

    /**
     * Returns statistics about detected entry points, grouped by class.
     */
    public Map<String, List<String>> getEntryPointStatistics(Set<GraphNode> entryPoints) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, List<String>> details = new HashMap<>();

        for (GraphNode node : entryPoints) {
            MethodInfo mi = node.getMethodInfo();
            String className = mi.getOwnerClass();
            String methodSig = mi.getMethodName() + mi.getDescriptor();

            counts.merge(className, 1, Integer::sum);
            details.computeIfAbsent(className, k -> new ArrayList<>()).add(methodSig);
        }

        Map<String, List<String>> result = new HashMap<>();
        List<String> countLines = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            countLines.add(e.getKey() + ": " + e.getValue() + " entry points");
        }
        result.put("counts", countLines);

        List<String> detailLines = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : details.entrySet()) {
            detailLines.add("Class: " + e.getKey());
            detailLines.addAll(e.getValue());
        }
        result.put("details", detailLines);

        return result;
    }
}