package org.example.analysis;

import org.example.model.MethodInfo;
import org.example.model.ReflectionCall;
import org.example.graph.CallGraph;
import org.example.graph.GraphNode;
import java.util.*;

public class ReflectionAnalyzer {

    public void analyzeReflection(List<MethodInfo> methods, CallGraph callGraph) {
        Map<String, List<ReflectionCall>> reflectionCallsByMethod = new HashMap<>();

        // Collect all reflection calls
        for (MethodInfo method : methods) {
            if (!method.getReflectionCalls().isEmpty()) {
                reflectionCallsByMethod.put(method.getFullName(), method.getReflectionCalls());
            }
        }

        // Process each reflection call
        for (Map.Entry<String, List<ReflectionCall>> entry : reflectionCallsByMethod.entrySet()) {
            String caller = entry.getKey();
            for (ReflectionCall reflection : entry.getValue()) {
                processReflectionCall(caller, reflection, callGraph, methods);
            }
        }
    }

    private void processReflectionCall(String caller, ReflectionCall reflection,
                                       CallGraph callGraph, List<MethodInfo> methods) {
        switch (reflection.getReflectionType()) {
            case "CLASS_FOR_NAME":
                handleClassForName(caller, reflection, callGraph, methods);
                break;
            case "GET_METHOD":
                handleGetMethod(caller, reflection, callGraph, methods);
                break;
            case "METHOD_INVOKE":
                handleMethodInvoke(caller, reflection, callGraph, methods);
                break;
        }
    }

    private void handleClassForName(String caller, ReflectionCall reflection,
                                    CallGraph callGraph, List<MethodInfo> methods) {
        // Find string constants in the caller method
        MethodInfo callerMethod = findMethodByFullName(methods, caller);
        if (callerMethod != null) {
            for (String constant : callerMethod.getStringConstants()) {
                // Check if it looks like a class name
                if (isClassName(constant)) {
                    // Add all public methods of this class as potential targets
                    String className = constant.replace("/", ".");
                    addClassMethodsAsTargets(className, caller, callGraph, methods);
                }
            }
        }
    }

    private void handleGetMethod(String caller, ReflectionCall reflection,
                                 CallGraph callGraph, List<MethodInfo> methods) {
        MethodInfo callerMethod = findMethodByFullName(methods, caller);
        if (callerMethod != null) {
            // Look for method name patterns in string constants
            for (String constant : callerMethod.getStringConstants()) {
                if (isMethodNamePattern(constant)) {
                    // Try to find matching methods
                    findAndLinkMatchingMethods(constant, caller, callGraph, methods);
                }
            }
        }
    }

    private void handleMethodInvoke(String caller, ReflectionCall reflection,
                                    CallGraph callGraph, List<MethodInfo> methods) {
        // For invoke calls, we need to trace back to getMethod calls
        // This is complex - we'll use a simplified approach
        MethodInfo callerMethod = findMethodByFullName(methods, caller);
        if (callerMethod != null) {
            // Look for patterns like "methodName" or "set" + Something
            for (String constant : callerMethod.getStringConstants()) {
                if (constant.matches("[a-zA-Z_$][a-zA-Z\\d_$]*")) {
                    // Could be a method name
                    linkPotentialMethods(constant, caller, callGraph, methods);
                }
            }
        }
    }

    private void addClassMethodsAsTargets(String className, String caller,
                                          CallGraph callGraph, List<MethodInfo> methods) {
        // Find all methods from this class
        for (MethodInfo method : methods) {
            if (method.getOwnerClass().equals(className)) {
                String target = method.getFullName();
                GraphNode targetNode = callGraph.getNode(target);
                GraphNode callerNode = callGraph.getNode(caller);

                if (targetNode != null && callerNode != null) {
                    // Add reflection-based edge
                    callGraph.addCall(caller, target);
                    System.out.println("[Reflection] Added edge: " + caller + " -> " + target);
                }
            }
        }
    }

    private void findAndLinkMatchingMethods(String methodPattern, String caller,
                                            CallGraph callGraph, List<MethodInfo> methods) {
        for (MethodInfo method : methods) {
            if (method.getMethodName().contains(methodPattern) ||
                    method.getMethodName().equals(methodPattern)) {

                String target = method.getFullName();
                GraphNode targetNode = callGraph.getNode(target);
                GraphNode callerNode = callGraph.getNode(caller);

                if (targetNode != null && callerNode != null) {
                    callGraph.addCall(caller, target);
                    System.out.println("[Reflection] Linked: " + caller + " -> " + target);
                }
            }
        }
    }

    private void linkPotentialMethods(String methodName, String caller,
                                      CallGraph callGraph, List<MethodInfo> methods) {
        // Simple heuristic: link to methods with same name in any class
        for (MethodInfo method : methods) {
            if (method.getMethodName().equals(methodName)) {
                String target = method.getFullName();
                GraphNode targetNode = callGraph.getNode(target);
                GraphNode callerNode = callGraph.getNode(caller);

                if (targetNode != null && callerNode != null) {
                    callGraph.addCall(caller, target);
                }
            }
        }
    }

    private MethodInfo findMethodByFullName(List<MethodInfo> methods, String fullName) {
        for (MethodInfo method : methods) {
            if (method.getFullName().equals(fullName)) {
                return method;
            }
        }
        return null;
    }

    private boolean isClassName(String str) {
        // Simple heuristic for class names
        return str.contains("/") || str.contains(".") &&
                str.matches("[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*");
    }

    private boolean isMethodNamePattern(String str) {
        return str.matches("[a-zA-Z_$][a-zA-Z\\d_$]*") &&
                !str.isEmpty() &&
                !str.matches("^[A-Z_]+$"); // Not all caps (likely constant)
    }

    public Map<String, List<String>> getReflectionSummary(List<MethodInfo> methods) {
        Map<String, List<String>> summary = new HashMap<>();

        for (MethodInfo method : methods) {
            if (!method.getReflectionCalls().isEmpty()) {
                List<String> calls = new ArrayList<>();
                for (ReflectionCall call : method.getReflectionCalls()) {
                    calls.add(call.toString());
                }
                summary.put(method.getFullName(), calls);
            }
        }

        return summary;
    }
}