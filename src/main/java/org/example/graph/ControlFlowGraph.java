package org.example.graph;

import org.example.model.InstructionInfo;
import org.example.model.MethodInfo;
import java.util.*;

public class ControlFlowGraph {
    private MethodInfo methodInfo;
    private Map<Integer, Set<Integer>> edges; // From instruction index to target indices
    private Set<Integer> basicBlockStarts;

    public ControlFlowGraph(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        this.edges = new HashMap<>();
        this.basicBlockStarts = new HashSet<>();
        buildGraph();
    }

    private void buildGraph() {
        List<InstructionInfo> instructions = methodInfo.getInstructions();

        // Add sequential flow edges
        for (int i = 0; i < instructions.size() - 1; i++) {
            addEdge(i, i + 1);
        }

        // Add control flow edges
        for (int i = 0; i < instructions.size(); i++) {
            InstructionInfo instr = instructions.get(i);
            if (instr.isControlFlow()) {
                // For jump instructions, we need to identify targets
                // In a real implementation, you'd parse the actual label targets
                // This is a simplified version
                int targetIndex = instr.getTargetIndex();
                if (targetIndex >= 0 && targetIndex < instructions.size()) {
                    addEdge(i, targetIndex);
                }
            }
        }

        // Identify basic block starts
        identifyBasicBlocks();
    }

    private void addEdge(int from, int to) {
        edges.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    private void identifyBasicBlocks() {
        basicBlockStarts.add(0); // First instruction starts a basic block

        for (int i = 0; i < methodInfo.getInstructions().size(); i++) {
            InstructionInfo instr = methodInfo.getInstructions().get(i);
            if (instr.isControlFlow()) {
                // Next instruction after a control flow is a new basic block
                if (i + 1 < methodInfo.getInstructions().size()) {
                    basicBlockStarts.add(i + 1);
                }
                // Target of control flow is also a basic block start
                int targetIndex = instr.getTargetIndex();
                if (targetIndex >= 0) {
                    basicBlockStarts.add(targetIndex);
                }
            }
        }
    }

    public Set<Integer> findReachableInstructions(int startIndex) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(startIndex);
        visited.add(startIndex);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            Set<Integer> neighbors = edges.get(current);

            if (neighbors != null) {
                for (int neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return visited;
    }

    public Set<Integer> findUnreachableInstructions(int startIndex) {
        Set<Integer> reachable = findReachableInstructions(startIndex);
        Set<Integer> allIndices = new HashSet<>();

        for (int i = 0; i < methodInfo.getInstructions().size(); i++) {
            allIndices.add(i);
        }

        allIndices.removeAll(reachable);
        return allIndices;
    }

    public Set<Integer> getBasicBlockStarts() {
        return basicBlockStarts;
    }

    public Map<Integer, Set<Integer>> getEdges() {
        return edges;
    }
}