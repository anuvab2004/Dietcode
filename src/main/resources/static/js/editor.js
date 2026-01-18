// Code editor functionality

let codeEditor;

// Initialize CodeMirror
document.addEventListener('DOMContentLoaded', () => {
    const textarea = document.getElementById('codeEditor');
    codeEditor = CodeMirror.fromTextArea(textarea, {
        mode: 'text/x-java',
        theme: 'dracula',
        lineNumbers: true,
        indentUnit: 4,
        tabSize: 4,
        indentWithTabs: false,
        lineWrapping: true,
        autocorrect: true,
        extraKeys: {
            "Tab": cm => cm.replaceSelection("    ", "end")
        }
    });

    // Set initial size
    codeEditor.setSize('100%', '400px');
});

// Reset to example code
function resetToExample() {
    const exampleCode = `public class Example {
    // This method will be called
    public static void main(String[] args) {
        liveMethod();
    }

    // Live method - called from main
    public static void liveMethod() {
        System.out.println("This method is used");
    }

    // Dead method - never called
    public static void deadMethod() {
        System.out.println("This code is dead");
    }

    // Write-only field
    private String unusedField = "test";

    // Used field
    private int usedField = 10;

    public void useFields() {
        // Only writing to unusedField, never reading
        unusedField = "changed";

        // Both read and write
        usedField = usedField * 2;
    }
}`;
    codeEditor.setValue(exampleCode);
    document.getElementById('className').value = 'Example';
    CommonUtils.showNotification('Reset to example code', 'info');
}

// Analyze code
async function analyzeCode() {
    const code = codeEditor.getValue();
    const className = document.getElementById('className').value.trim();

    if (!code.trim()) {
        showEditorError('Please enter some Java code to analyze.');
        return;
    }

    if (!className) {
        showEditorError('Please enter a class name.');
        return;
    }

    const analyzeBtn = document.getElementById('analyzeCodeBtn');
    const spinner = document.getElementById('codeSpinner');

    // Show loading state
    analyzeBtn.disabled = true;
    spinner.style.display = 'inline-block';
    hideEditorError();
    hideEditorResults();

    // Prepare request data
    const requestData = {
        code,
        className,
        includeReflection: document.getElementById('editorReflection').checked,
        includeFields: document.getElementById('editorFields').checked,
        includeDeadBlocks: document.getElementById('editorDeadBlocks').checked
    };

    try {
        const response = await fetch('/api/analyze/code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        });

        const result = await response.json();

        if (result.success) {
            displayEditorResults(result);
            CommonUtils.showNotification('Code analyzed successfully!', 'success');
        } else {
            showEditorError(result.error || 'Code analysis failed. Please check your Java syntax.');
        }
    } catch (error) {
        showEditorError('Network error: ' + error.message);
        console.error('Analysis error:', error);
    } finally {
        // Reset button state
        analyzeBtn.disabled = false;
        spinner.style.display = 'none';
    }
}

// Display editor results
function displayEditorResults(data) {
    const container = document.getElementById('editorResultsContainer');
    const resultsSection = document.getElementById('editorResults');

    const report = data.report || {};
    const summary = data.summary || {};

    container.innerHTML = `
        <div class="result-summary">
            <div class="summary-card methods">
                <h3>${report.totalMethodsAnalyzed || 0}</h3>
                <p>Methods Analyzed</p>
            </div>
            <div class="summary-card dead-methods">
                <h3>${report.totalDeadMethods || 0}</h3>
                <p>Dead Methods</p>
            </div>
            <div class="summary-card dead-fields">
                <h3>${report.totalDeadFields || 0}</h3>
                <p>Dead Fields</p>
            </div>
            <div class="summary-card">
                <h3>${report.totalDeadBlocks || 0}</h3>
                <p>Dead Blocks</p>
            </div>
        </div>

        <div class="result-details">
            <div class="detail-section">
                <h4><i class="fas fa-info-circle"></i> Summary</h4>
                <p class="subtitle">Analysis of your Java code revealed:</p>
                <ul class="detail-list">
                    ${Object.values(summary).map(item => `<li>${item}</li>`).join('')}
                </ul>
            </div>

            <div class="detail-section">
                <h4><i class="fas fa-lightbulb"></i> Recommendations</h4>
                <ul class="detail-list">
                    ${report.totalDeadMethods > 0
                        ? `<li>Consider removing ${report.totalDeadMethods} unused method${report.totalDeadMethods > 1 ? 's' : ''}</li>`
                        : `<li>Great! No dead methods found</li>`}
                    ${report.totalDeadFields > 0
                        ? `<li>Remove ${report.totalDeadFields} unused field${report.totalDeadFields > 1 ? 's' : ''}</li>`
                        : `<li>All fields are properly used</li>`}
                    <li>Review reflection usage if applicable</li>
                </ul>
            </div>
        </div>

        <div class="action-buttons">
            <button class="btn btn-primary" onclick="downloadEditorReport()">
                <i class="fas fa-download"></i> Download Analysis Report
            </button>
            <button class="btn btn-secondary" onclick="shareAnalysis()">
                <i class="fas fa-share"></i> Share Results
            </button>
        </div>
    `;

    resultsSection.style.display = 'block';
    resultsSection.scrollIntoView({ behavior: 'smooth' });
}

// Download editor report
function downloadEditorReport() {
    CommonUtils.showNotification('Preparing download...', 'info');
    alert('Download feature will be implemented with backend integration');
}

// Share analysis
function shareAnalysis() {
    if (navigator.share) {
        navigator.share({
            title: 'Java Dead Code Analysis',
            text: 'Check out my Java code analysis results!',
            url: window.location.href
        });
    } else {
        navigator.clipboard.writeText(window.location.href);
        CommonUtils.showNotification('Link copied to clipboard!', 'success');
    }
}

// Error handling for editor
function showEditorError(message) {
    const errorAlert = document.getElementById('editorError');
    errorAlert.textContent = message;
    errorAlert.style.display = 'block';
    errorAlert.scrollIntoView({ behavior: 'smooth' });
}

function hideEditorError() {
    document.getElementById('editorError').style.display = 'none';
}

function hideEditorResults() {
    document.getElementById('editorResults').style.display = 'none';
}