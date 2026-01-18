package org.example.analysis;

import org.example.graph.CallGraph;
import org.example.graph.GraphNode;
import org.example.model.ClassInfo;
import org.example.model.MethodInfo;

import java.util.*;

public class DeadMethodAnalyzer {

    private final ReachabilityAnalyzer reachabilityAnalyzer;

    public DeadMethodAnalyzer() {
        this.reachabilityAnalyzer = new ReachabilityAnalyzer();
    }

    public List<MethodInfo> findDeadMethods(CallGraph callGraph, List<ClassInfo> classes) {
        EntryPointResolver resolver = new EntryPointResolver();
        Set<GraphNode> entryPoints = resolver.resolveEntryPoints(callGraph);

        if (entryPoints == null || entryPoints.isEmpty()) {
            System.out.println("Warning: No entry points found.");
            List<MethodInfo> all = getAllMethods(callGraph);
            return filterOutSpecialMethods(all, resolver);
        }

        Set<GraphNode> reachable = new HashSet<>();

        for (GraphNode entry : entryPoints) {
            Set<GraphNode> deadFromEntry = reachabilityAnalyzer.findDeadMethods(callGraph, entry);
            Set<GraphNode> allNodes = new HashSet<>(callGraph.getAllNodes());
            Set<GraphNode> reachableFromEntry = new HashSet<>(allNodes);
            reachableFromEntry.removeAll(deadFromEntry);
            reachable.addAll(reachableFromEntry);
        }

        Set<GraphNode> allNodes = new HashSet<>(callGraph.getAllNodes());
        Set<GraphNode> candidateDead = new HashSet<>(allNodes);
        candidateDead.removeAll(reachable);

        List<MethodInfo> deadMethods = new ArrayList<>();
        for (GraphNode node : candidateDead) {
            deadMethods.add(node.getMethodInfo());
        }

        // Remove special/protected methods
        return filterOutSpecialMethods(deadMethods, resolver);
    }

    private List<MethodInfo> filterOutSpecialMethods(List<MethodInfo> methods, EntryPointResolver resolver) {
        List<MethodInfo> filtered = new ArrayList<>();

        for (MethodInfo m : methods) {
            if (!isSpecialMethod(m, resolver)) {
                filtered.add(m);
            }
        }

        return filtered;
    }

    private boolean isSpecialMethod(MethodInfo method, EntryPointResolver resolver) {
        String name = method.getMethodName();
        int access = method.getAccessFlags();

        if ("<clinit>".equals(name)) {
            return true;
        }

        if ("<init>".equals(name) && resolver.shouldExcludeFromDead(method)) {
            return true;
        }

        // Synthetic / bridge methods
        if ((access & 0x1000) != 0 || (access & 0x0040) != 0) {
            return true;
        }

        return false;
    }

    public List<MethodInfo> findDeadMethodsSimple(CallGraph callGraph) {
        EntryPointResolver resolver = new EntryPointResolver();
        Set<GraphNode> entryPoints = resolver.resolveEntryPoints(callGraph);

        if (entryPoints == null || entryPoints.isEmpty()) {
            System.out.println("Warning: No entry points found.");
            List<MethodInfo> all = getAllMethods(callGraph);
            return filterOutSpecialMethods(all, resolver);
        }

        GraphNode first = entryPoints.iterator().next();
        Set<GraphNode> deadNodes = reachabilityAnalyzer.findDeadMethods(callGraph, first);

        List<MethodInfo> deadMethods = new ArrayList<>();
        for (GraphNode node : deadNodes) {
            deadMethods.add(node.getMethodInfo());
        }

        return filterOutSpecialMethods(deadMethods, resolver);
    }

    public Map<String, List<String>> categorizeDeadMethods(List<MethodInfo> deadMethods) {
        Map<String, List<String>> result = new HashMap<>();

        for (MethodInfo m : deadMethods) {
            String className = m.getOwnerClass();
            String signature = m.getMethodName() + m.getDescriptor();

            result.computeIfAbsent(className, k -> new ArrayList<>()).add(signature);
        }

        return result;
    }

    public void printDeadMethods(List<MethodInfo> deadMethods) {
        if (deadMethods.isEmpty()) {
            System.out.println("No dead methods found.");
            return;
        }

        System.out.println("\n=== Dead Methods (" + deadMethods.size() + ") ===");

        Map<String, List<MethodInfo>> byClass = new TreeMap<>();
        for (MethodInfo m : deadMethods) {
            byClass.computeIfAbsent(m.getOwnerClass(), k -> new ArrayList<>()).add(m);
        }

        for (Map.Entry<String, List<MethodInfo>> entry : byClass.entrySet()) {
            System.out.println("\nClass: " + entry.getKey());
            for (MethodInfo m : entry.getValue()) {
                System.out.println("  - " + m.getMethodName() + m.getDescriptor());
            }
        }
    }

    private List<MethodInfo> getAllMethods(CallGraph callGraph) {
        List<MethodInfo> methods = new ArrayList<>();
        for (GraphNode node : callGraph.getAllNodes()) {
            methods.add(node.getMethodInfo());
        }
        return methods;
    }
}