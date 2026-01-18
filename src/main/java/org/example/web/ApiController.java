package org.example.web;

import org.example.Main;
import org.example.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.FileWriter;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private static final String UPLOAD_DIR = "uploads/";

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Java Bytecode Dead Code Analyzer API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Analyze uploaded bytecode file (.class, .jar, or directory archive)
     */
    @PostMapping("/analyze/upload")
    public ResponseEntity<Map<String, Object>> analyzeUpload(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("error", "No file uploaded");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if file is a valid bytecode file
        String originalName = file.getOriginalFilename();
        if (originalName == null ||
                (!originalName.endsWith(".class") &&
                        !originalName.endsWith(".jar") &&
                        !originalName.endsWith(".zip") &&
                        !originalName.endsWith(".war") &&
                        !originalName.endsWith(".ear"))) {
            response.put("success", false);
            response.put("error", "File must be a .class, .jar, .zip, .war, or .ear file");
            return ResponseEntity.badRequest().body(response);
        }

        Path uploadDir = Paths.get(UPLOAD_DIR);
        Path tempDir = null;

        try {
            // Create upload directory if it doesn't exist
            Files.createDirectories(uploadDir);

            // Create a unique temporary directory for this analysis
            String uniqueId = UUID.randomUUID().toString();
            tempDir = uploadDir.resolve(uniqueId);
            Files.createDirectories(tempDir);

            // Save the uploaded file
            String safeFilename = originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
            Path uploadedFile = tempDir.resolve(safeFilename);
            Files.copy(file.getInputStream(), uploadedFile, StandardCopyOption.REPLACE_EXISTING);

            // Perform bytecode analysis
            Report report = Main.analyzeAndGetReport(uploadedFile.toString());

            response.put("success", true);
            response.put("message", "Bytecode file analyzed successfully");
            response.put("originalFilename", originalName);
            response.put("fileSize", file.getSize());
            response.put("fileType", getFileType(originalName));
            response.put("report", convertReportToMap(report));
            response.put("summary", createSummary(report));

        } catch (IOException e) {
            logger.error("File processing failed", e);
            response.put("success", false);
            response.put("error", "File processing failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Analysis failed", e);
            response.put("success", false);
            response.put("error", "Analysis failed: " + e.getMessage());
        } finally {
            // Clean up temporary files
            cleanupTempFiles(tempDir);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Analyze Java source code from editor
     * Compiles the code and then analyzes the resulting bytecode
     */
    @PostMapping("/analyze/code")
    public ResponseEntity<Map<String, Object>> analyzeCode(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        String code = (String) request.get("code");
        String className = (String) request.get("className");

        if (code == null || code.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "No code provided");
            return ResponseEntity.badRequest().body(response);
        }

        if (className == null || className.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Class name is required");
            return ResponseEntity.badRequest().body(response);
        }

        Path tempDir = null;
        Path sourceFile = null;

        try {
            // Create temporary directory for compilation
            tempDir = Files.createTempDirectory("java-compile-");
            sourceFile = tempDir.resolve(className + ".java");

            // Write source code to file
            try (FileWriter writer = new FileWriter(sourceFile.toFile())) {
                writer.write(code);
            }

            // Compile Java source code
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                response.put("success", false);
                response.put("error", "Java compiler not available. Ensure JDK (not JRE) is installed.");
                return ResponseEntity.status(500).body(response);
            }

            int compilationResult = compiler.run(
                    null, null, null,
                    sourceFile.toString()
            );

            if (compilationResult != 0) {
                response.put("success", false);
                response.put("error", "Compilation failed. Please check your Java syntax.");
                return ResponseEntity.badRequest().body(response);
            }

            // Analyze the compiled bytecode
            Report report = Main.analyzeAndGetReport(tempDir.toString());

            response.put("success", true);
            response.put("message", "Code compiled and analyzed successfully");
            response.put("report", convertReportToMap(report));
            response.put("summary", createSummary(report));

        } catch (IOException e) {
            logger.error("File processing failed", e);
            response.put("success", false);
            response.put("error", "File processing failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Analysis failed", e);
            response.put("success", false);
            response.put("error", "Analysis failed: " + e.getMessage());
        } finally {
            // Clean up temporary files
            cleanupTempFiles(tempDir);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to verify the API is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "API is working!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    /**
     * Get API information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Java Bytecode Dead Code Analyzer");
        info.put("version", "1.0.0");
        info.put("description", "Analyzes Java bytecode for dead code, unused fields, and reflection usage");
                info.put("endpoints", List.of(
                "GET /api/health - Health check",
                "GET /api/info - API information",
                "GET /api/test - Test endpoint",
                "POST /api/analyze/upload - Upload and analyze bytecode file",
                "POST /api/analyze/code - Compile and analyze Java source code"
        ));
        
        info.put("supportedFileTypes", List.of(".class", ".jar", ".zip", ".war", ".ear"));
        return ResponseEntity.ok(info);
    }

    // ── Helper Methods ─────────────────────────────────────────────────────────

    private String getFileType(String filename) {
        if (filename.endsWith(".class")) return "Java Class File";
        if (filename.endsWith(".jar")) return "Java Archive (JAR)";
        if (filename.endsWith(".war")) return "Web Archive (WAR)";
        if (filename.endsWith(".ear")) return "Enterprise Archive (EAR)";
        if (filename.endsWith(".zip")) return "ZIP Archive";
        return "Unknown";
    }

    private void cleanupTempFiles(Path tempDir) {
        if (tempDir != null && Files.exists(tempDir)) {
            try (Stream<Path> paths = Files.walk(tempDir)) {
                // Delete all files in the directory
                paths.sorted((a, b) -> -a.compareTo(b)) // reverse for directory-first deletion
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                logger.warn("Failed to delete: {}", path, e);
                            }
                        });
            } catch (IOException e) {
                logger.error("Failed to cleanup temp directory", e);
            }
        }
    }

    private Map<String, Object> convertReportToMap(Report report) {
        Map<String, Object> map = new HashMap<>();
        map.put("totalMethodsAnalyzed", report.getTotalMethodsAnalyzed());
        map.put("totalDeadMethods", report.getTotalDeadMethods());
        map.put("totalDeadFields", report.getTotalDeadFields());
        map.put("totalDeadBlocks", report.getTotalDeadBlocks());
        map.put("totalReflectionCalls", report.getTotalReflectionCalls());

        return map;
    }

    private Map<String, String> createSummary(Report report) {
        Map<String, String> summary = new HashMap<>();
        int total = report.getTotalMethodsAnalyzed();

        summary.put("methods", total + " methods analyzed");
        summary.put("deadMethods", report.getTotalDeadMethods() + " dead methods found");
        summary.put("deadFields", report.getTotalDeadFields() + " dead fields found");
        summary.put("deadBlocks", report.getTotalDeadBlocks() + " dead code blocks found");
        summary.put("reflection", report.getTotalReflectionCalls() + " reflection calls detected");

        if (total > 0) {
            double percentage = (double) report.getTotalDeadMethods() / total * 100;
            summary.put("deadCodePercentage", String.format("%.1f%% dead code", percentage));
        } else {
            summary.put("deadCodePercentage", "0.0% dead code");
        }

        return summary;
    }
}