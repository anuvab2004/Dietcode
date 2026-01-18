package org.example.analysis;

import org.example.graph.ControlFlowGraph;
import org.example.model.InstructionInfo;
import org.example.model.MethodInfo;
import java.util.*;

public class DeadBlockAnalyzer {

    public Map<String, Set<Integer>> findDeadBlocks(MethodInfo methodInfo) {
        Map<String, Set<Integer>> deadBlocks = new HashMap<>();

        ControlFlowGraph cfg = new ControlFlowGraph(methodInfo);
        Set<Integer> unreachableInstructions = cfg.findUnreachableInstructions(0);

        if (!unreachableInstructions.isEmpty()) {
            String methodKey = methodInfo.getFullName();
            deadBlocks.put(methodKey, unreachableInstructions);
        }

        return deadBlocks;
    }

    public Map<String, Map<String, Set<Integer>>> findDeadBlocksInMethods(List<MethodInfo> methods) {
        Map<String, Map<String, Set<Integer>>> results = new HashMap<>();

        for (MethodInfo method : methods) {
            Map<String, Set<Integer>> deadBlocks = findDeadBlocks(method);

            for (Map.Entry<String, Set<Integer>> entry : deadBlocks.entrySet()) {
                String className = method.getOwnerClass();
                results.putIfAbsent(className, new HashMap<>());
                results.get(className).put(entry.getKey(), entry.getValue());
            }
        }

        return results;
    }

    public List<String> getDeadBlockDescriptions(MethodInfo methodInfo, Set<Integer> deadIndices) {
        List<String> descriptions = new ArrayList<>();
        List<InstructionInfo> instructions = methodInfo.getInstructions();

        for (int index : deadIndices) {
            if (index < instructions.size()) {
                InstructionInfo instr = instructions.get(index);
                descriptions.add(String.format("Instruction %d: opcode=%d, type=%s",
                        index, instr.getOpcode(), instr.getInstructionType()));
            }
        }

        return descriptions;
    }
}