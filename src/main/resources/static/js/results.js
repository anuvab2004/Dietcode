// Results page functionality

// Global state
let currentReport = null;
let currentSummary = null;

// Initialize page
document.addEventListener('DOMContentLoaded', () => {
    loadResults();
});

// Load results from URL parameters, local storage, or API
async function loadResults() {
    const loadingState = document.getElementById('loadingState');
    const errorState = document.getElementById('errorState');
    const resultsContainer = document.getElementById('resultsContainer');
    const errorMessage = document.getElementById('errorMessage');

    try {
        // Try to load from URL parameters first
        const urlParams = new URLSearchParams(window.location.search);
        const reportId = urlParams.get('id');
        const dataParam = urlParams.get('data');

        if (dataParam) {
            // Decode data from URL
            const decoded = JSON.parse(decodeURIComponent(dataParam));
            currentReport = decoded.report || {};
            currentSummary = decoded.summary || {};
            displayResults();
            return;
        }

        if (reportId) {
            // Try to fetch from API (if you implement a results endpoint)
            const response = await fetch(`/api/results/${reportId}`);
            if (response.ok) {
                const data = await response.json();
                currentReport = data.report || {};
                currentSummary = data.summary || {};
                displayResults();
                return;
            }
        }

        // Try to load from local storage
        const storedData = localStorage.getItem('lastAnalysisResult');
        if (storedData) {
            const data = JSON.parse(storedData);
            currentReport = data.report || {};
            currentSummary = data.summary || {};
            displayResults();
            return;
        }

        // No data found
        throw new Error('No analysis results found. Please run an analysis first.');

    } catch (error) {
        console.error('Error loading results:', error);
        loadingState.style.display = 'none';
        errorState.style.display = 'block';
        errorMessage.textContent = error.message || 'Failed to load analysis results.';
    }
}

// Display results on the page
function displayResults() {
    const loadingState = document.getElementById('loadingState');
    const errorState = document.getElementById('errorState');
    const resultsContainer = document.getElementById('resultsContainer');

    loadingState.style.display = 'none';
    errorState.style.display = 'none';
    resultsContainer.style.display = 'block';

    const report = currentReport || {};
    const summary = currentSummary || {};

    // Display summary cards
    displaySummaryCards(report);

    // Display detailed results
    displayDetailedResults(report, summary);

    // Display action buttons
    displayActionButtons(report);
}

// Display summary cards
function displaySummaryCards(report) {
    const summaryCards = document.getElementById('summaryCards');
    
    summaryCards.innerHTML = `
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
    `;
}

// Display detailed results
function displayDetailedResults(report, summary) {
    const detailsContainer = document.getElementById('detailsContainer');

    let html = '';

    // Summary section
    if (Object.keys(summary).length > 0) {
        html += `
            <div class="detail-section">
                <h4><i class="fas fa-info-circle"></i> Summary</h4>
                <ul class="detail-list">
                    ${Object.values(summary).map(item => `<li>${item}</li>`).join('')}
                </ul>
            </div>
        `;
    }

    // Dead Methods section
    if (report.deadMethods && report.deadMethods.length > 0) {
        html += `
            <div class="detail-section">
                <h4><i class="fas fa-exclamation-triangle"></i> Dead Methods (${report.deadMethods.length})</h4>
                <div class="methods-list">
                    ${report.deadMethods.map(method => `
                        <div class="method-item">
                            <div class="method-header">
                                <span class="method-name">${escapeHtml(method.methodName || 'Unknown')}</span>
                                <span class="method-signature">${escapeHtml(method.signature || '')}</span>
                            </div>
                            <div class="method-info">
                                <span class="method-class"><i class="fas fa-file-code"></i> ${escapeHtml(method.className || 'Unknown')}</span>
                                ${method.accessFlags ? `<span class="access-flags">${formatAccessFlags(method.accessFlags)}</span>` : ''}
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    // Dead Fields section
    if (report.deadFields && report.deadFields.length > 0) {
        html += `
            <div class="detail-section">
                <h4><i class="fas fa-database"></i> Dead Fields (${report.deadFields.length})</h4>
                <div class="fields-list">
                    ${report.deadFields.map(field => `
                        <div class="field-item">
                            <div class="field-header">
                                <span class="field-name">${escapeHtml(field.fieldName || 'Unknown')}</span>
                                <span class="field-type">${escapeHtml(field.type || 'Unknown')}</span>
                            </div>
                            <div class="field-info">
                                <span class="field-class"><i class="fas fa-file-code"></i> ${escapeHtml(field.className || 'Unknown')}</span>
                                ${field.accessFlags ? `<span class="access-flags">${formatAccessFlags(field.accessFlags)}</span>` : ''}
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    // Dead Blocks section
    if (report.deadBlocks && Object.keys(report.deadBlocks).length > 0) {
        html += `
            <div class="detail-section">
                <h4><i class="fas fa-code-branch"></i> Dead Code Blocks</h4>
                <div class="blocks-list">
                    ${Object.entries(report.deadBlocks).map(([methodKey, blocks]) => `
                        <div class="block-item">
                            <div class="block-method">${escapeHtml(methodKey)}</div>
                            <div class="block-lines">Dead blocks at lines: ${Array.from(blocks).join(', ')}</div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    // Reflection Analysis section
    if (report.totalReflectionCalls && report.totalReflectionCalls > 0) {
        html += `
            <div class="detail-section">
                <h4><i class="fas fa-magic"></i> Reflection Analysis</h4>
                <p>Found ${report.totalReflectionCalls} reflection call${report.totalReflectionCalls > 1 ? 's' : ''} in the codebase.</p>
                <p class="subtitle">Methods called via reflection may appear as dead code but are actually used.</p>
            </div>
        `;
    }

    // Recommendations section
    html += `
        <div class="detail-section">
            <h4><i class="fas fa-lightbulb"></i> Recommendations</h4>
            <ul class="detail-list">
                ${report.totalDeadMethods > 0
                    ? `<li>Consider removing ${report.totalDeadMethods} unused method${report.totalDeadMethods > 1 ? 's' : ''} to reduce code complexity and maintenance overhead</li>`
                    : `<li>Great! No dead methods found. Your codebase is clean.</li>`}
                ${report.totalDeadFields > 0
                    ? `<li>Remove ${report.totalDeadFields} unused field${report.totalDeadFields > 1 ? 's' : ''} to free up memory and reduce confusion</li>`
                    : `<li>All fields are properly used. Well done!</li>`}
                ${report.totalDeadBlocks > 0
                    ? `<li>Review ${report.totalDeadBlocks} dead code block${report.totalDeadBlocks > 1 ? 's' : ''} - these may indicate unreachable code paths</li>`
                    : `<li>No dead code blocks detected. Excellent code quality!</li>`}
                <li>Before removing code, ensure it's not called via reflection or external APIs</li>
                <li>Consider using version control to track changes when cleaning up dead code</li>
            </ul>
        </div>
    `;

    detailsContainer.innerHTML = html;
}

// Display action buttons
function displayActionButtons(report) {
    const actionButtons = document.getElementById('actionButtons');

    actionButtons.innerHTML = `
        <button class="btn btn-primary" onclick="downloadReport('json')">
            <i class="fas fa-download"></i> Download JSON Report
        </button>
        <button class="btn btn-secondary" onclick="downloadReport('html')">
            <i class="fas fa-download"></i> Download HTML Report
        </button>
        <button class="btn btn-secondary" onclick="shareResults()">
            <i class="fas fa-share"></i> Share Results
        </button>
        <button class="btn btn-secondary" onclick="newAnalysis()">
            <i class="fas fa-plus"></i> New Analysis
        </button>
    `;
}

// Download report
function downloadReport(format) {
    if (!currentReport) {
        CommonUtils.showNotification('No report data available', 'error');
        return;
    }

    const report = currentReport;
    const summary = currentSummary || {};

    if (format === 'json') {
        const data = {
            report,
            summary,
            generatedAt: new Date().toISOString(),
            version: '1.0.0'
        };

        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `dead-code-analysis-${Date.now()}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        CommonUtils.showNotification('JSON report downloaded successfully!', 'success');
    } else if (format === 'html') {
        // Generate HTML report
        const html = generateHtmlReport(report, summary);
        const blob = new Blob([html], { type: 'text/html' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `dead-code-analysis-${Date.now()}.html`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        CommonUtils.showNotification('HTML report downloaded successfully!', 'success');
    }
}

// Generate HTML report
function generateHtmlReport(report, summary) {
    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dead Code Analysis Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
        h1 { color: #333; }
        .summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin: 20px 0; }
        .summary-card { padding: 20px; border: 1px solid #ddd; border-radius: 8px; text-align: center; }
        .detail-section { margin: 30px 0; }
        .detail-section h2 { color: #666; border-bottom: 2px solid #ddd; padding-bottom: 10px; }
        .method-item, .field-item { padding: 10px; margin: 10px 0; background: #f9f9f9; border-radius: 4px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #4CAF50; color: white; }
    </style>
</head>
<body>
    <h1>Dead Code Analysis Report</h1>
    <p>Generated: ${new Date().toLocaleString()}</p>
    
    <div class="summary">
        <div class="summary-card">
            <h3>${report.totalMethodsAnalyzed || 0}</h3>
            <p>Methods Analyzed</p>
        </div>
        <div class="summary-card">
            <h3>${report.totalDeadMethods || 0}</h3>
            <p>Dead Methods</p>
        </div>
        <div class="summary-card">
            <h3>${report.totalDeadFields || 0}</h3>
            <p>Dead Fields</p>
        </div>
        <div class="summary-card">
            <h3>${report.totalDeadBlocks || 0}</h3>
            <p>Dead Blocks</p>
        </div>
    </div>

    ${Object.keys(summary).length > 0 ? `
    <div class="detail-section">
        <h2>Summary</h2>
        <ul>${Object.values(summary).map(item => `<li>${escapeHtml(item)}</li>`).join('')}</ul>
    </div>
    ` : ''}

    ${report.deadMethods && report.deadMethods.length > 0 ? `
    <div class="detail-section">
        <h2>Dead Methods (${report.deadMethods.length})</h2>
        <table>
            <tr><th>Class</th><th>Method</th><th>Signature</th></tr>
            ${report.deadMethods.map(m => `
                <tr>
                    <td>${escapeHtml(m.className || 'Unknown')}</td>
                    <td>${escapeHtml(m.methodName || 'Unknown')}</td>
                    <td>${escapeHtml(m.signature || '')}</td>
                </tr>
            `).join('')}
        </table>
    </div>
    ` : ''}

    ${report.deadFields && report.deadFields.length > 0 ? `
    <div class="detail-section">
        <h2>Dead Fields (${report.deadFields.length})</h2>
        <table>
            <tr><th>Class</th><th>Field</th><th>Type</th></tr>
            ${report.deadFields.map(f => `
                <tr>
                    <td>${escapeHtml(f.className || 'Unknown')}</td>
                    <td>${escapeHtml(f.fieldName || 'Unknown')}</td>
                    <td>${escapeHtml(f.type || 'Unknown')}</td>
                </tr>
            `).join('')}
        </table>
    </div>
    ` : ''}
</body>
</html>`;
}

// Share results
function shareResults() {
    if (!currentReport) {
        CommonUtils.showNotification('No report data available', 'error');
        return;
    }

    const data = {
        report: currentReport,
        summary: currentSummary
    };

    const encoded = encodeURIComponent(JSON.stringify(data));
    const shareUrl = `${window.location.origin}/results.html?data=${encoded}`;

    if (navigator.share) {
        navigator.share({
            title: 'Java Dead Code Analysis Results',
            text: 'Check out my Java code analysis results!',
            url: shareUrl
        });
    } else {
        navigator.clipboard.writeText(shareUrl);
        CommonUtils.showNotification('Share link copied to clipboard!', 'success');
    }
}

// Navigate to new analysis
function newAnalysis() {
    window.location.href = '/';
}

// Utility functions
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatAccessFlags(flags) {
    if (!flags) return '';
    const flagNames = [];
    if ((flags & 0x0001) !== 0) flagNames.push('public');
    if ((flags & 0x0002) !== 0) flagNames.push('private');
    if ((flags & 0x0004) !== 0) flagNames.push('protected');
    if ((flags & 0x0008) !== 0) flagNames.push('static');
    if ((flags & 0x0010) !== 0) flagNames.push('final');
    return flagNames.join(' ');
}
