package org.example.graph;

import org.example.model.MethodInfo;
import java.util.*;

public class CallGraph {
    private Map<String, GraphNode> nodes; // Key: method full name
    private Set<GraphNode> entryPoints; // Methods that can be entry points (e.g., main)

    public CallGraph() {
        this.nodes = new HashMap<>();
        this.entryPoints = new HashSet<>();
    }

    public void addMethod(MethodInfo methodInfo) {
        String nodeId = methodInfo.getFullName();
        if (!nodes.containsKey(nodeId)) {
            nodes.put(nodeId, new GraphNode(methodInfo));
        }
    }

    public void addCall(String callerId, String calleeId) {
        GraphNode caller = nodes.get(callerId);
        GraphNode callee = nodes.get(calleeId);

        if (caller != null && callee != null) {
            caller.addOutgoingCall(callee);
            callee.addIncomingCall(caller);
        }
    }

    public GraphNode getNode(String methodId) {
        return nodes.get(methodId);
    }

    public Collection<GraphNode> getAllNodes() {
        return nodes.values();
    }

    public Set<GraphNode> getEntryPoints() {
        return entryPoints;
    }

    public void addEntryPoint(GraphNode node) {
        entryPoints.add(node);
    }

    public Set<GraphNode> findReachableMethods(GraphNode startNode) {
        Set<GraphNode> visited = new HashSet<>();
        Queue<GraphNode> queue = new LinkedList<>();

        queue.add(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            GraphNode current = queue.poll();

            for (GraphNode neighbor : current.getOutgoingCalls()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }

    public Set<GraphNode> findUnreachableMethods(GraphNode startNode) {
        Set<GraphNode> reachable = findReachableMethods(startNode);
        Set<GraphNode> allNodes = new HashSet<>(nodes.values());
        allNodes.removeAll(reachable);
        return allNodes;
    }
}