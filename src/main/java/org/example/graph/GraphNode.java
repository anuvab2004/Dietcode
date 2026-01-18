package org.example.graph;

import org.example.model.MethodInfo;
import java.util.HashSet;
import java.util.Set;

public class GraphNode {
    private String nodeId; // Format: className.methodName descriptor
    private MethodInfo methodInfo;
    private Set<GraphNode> outgoingCalls; // Methods this method calls
    private Set<GraphNode> incomingCalls; // Methods that call this method

    public GraphNode(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        this.nodeId = methodInfo.getFullName();
        this.outgoingCalls = new HashSet<>();
        this.incomingCalls = new HashSet<>();
    }

    // Getters and setters
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public MethodInfo getMethodInfo() { return methodInfo; }
    public void setMethodInfo(MethodInfo methodInfo) { this.methodInfo = methodInfo; }

    public Set<GraphNode> getOutgoingCalls() { return outgoingCalls; }
    public void setOutgoingCalls(Set<GraphNode> outgoingCalls) { this.outgoingCalls = outgoingCalls; }

    public Set<GraphNode> getIncomingCalls() { return incomingCalls; }
    public void setIncomingCalls(Set<GraphNode> incomingCalls) { this.incomingCalls = incomingCalls; }

    public void addOutgoingCall(GraphNode target) {
        outgoingCalls.add(target);
    }

    public void addIncomingCall(GraphNode source) {
        incomingCalls.add(source);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GraphNode graphNode = (GraphNode) obj;
        return nodeId.equals(graphNode.nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }
}