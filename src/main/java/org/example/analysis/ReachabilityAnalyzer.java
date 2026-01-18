package org.example.analysis;

import org.example.graph.CallGraph;
import org.example.graph.GraphNode;
import java.util.*;

public class ReachabilityAnalyzer {

    public Set<GraphNode> analyzeReachability(CallGraph callGraph, GraphNode entryPoint) {
        if (entryPoint == null) {
            return new HashSet<>();
        }

        return callGraph.findReachableMethods(entryPoint);
    }

    public Set<GraphNode> findDeadMethods(CallGraph callGraph, GraphNode entryPoint) {
        if (entryPoint == null) {
            return new HashSet<>(callGraph.getAllNodes());
        }

        Set<GraphNode> reachable = analyzeReachability(callGraph, entryPoint);
        Set<GraphNode> allNodes = new HashSet<>(callGraph.getAllNodes());
        allNodes.removeAll(reachable);

        return allNodes;
    }

    public Map<String, Set<String>> analyzeTransitiveClosure(CallGraph callGraph, GraphNode entryPoint) {
        Map<String, Set<String>> closure = new HashMap<>();

        if (entryPoint != null) {
            traverseAndBuildClosure(entryPoint, closure, new HashSet<>());
        }

        return closure;
    }

    private void traverseAndBuildClosure(GraphNode node, Map<String, Set<String>> closure, Set<String> visited) {
        String nodeId = node.getNodeId();

        if (visited.contains(nodeId)) {
            return;
        }

        visited.add(nodeId);
        closure.putIfAbsent(nodeId, new HashSet<>());

        for (GraphNode callee : node.getOutgoingCalls()) {
            closure.get(nodeId).add(callee.getNodeId());
            traverseAndBuildClosure(callee, closure, visited);
        }
    }
}