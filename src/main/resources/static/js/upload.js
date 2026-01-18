// Upload page functionality

let selectedFiles = [];

// Initialize drag and drop
document.addEventListener('DOMContentLoaded', function() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');

    // Click to browse
    uploadArea.addEventListener('click', () => fileInput.click());

    // Drag and drop handlers
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        handleFiles(e.dataTransfer.files);
    });

    // File input change
    fileInput.addEventListener('change', (e) => {
        handleFiles(e.target.files);
    });
});

// Handle selected files
function handleFiles(files) {
    for (let file of files) {
        if (isValidFile(file)) {
            selectedFiles.push(file);
        }
    }
    updateFileList();
}

// Check if file is valid
function isValidFile(file) {
    const validExtensions = ['.jar', '.class', '.java'];
    return validExtensions.some(ext => file.name.toLowerCase().endsWith(ext));
}

// Update file list display
function updateFileList() {
    const fileListDiv = document.getElementById('fileList');
    const listContainer = document.getElementById('selectedFiles');

    if (selectedFiles.length > 0) {
        fileListDiv.style.display = 'block';
        listContainer.innerHTML = '';

        selectedFiles.forEach((file, index) => {
            const fileItem = document.createElement('div');
            fileItem.className = 'file-item';
            fileItem.innerHTML = `
                <div class="file-info">
                    <i class="fas fa-file-code file-icon"></i>
                    <div>
                        <strong>${file.name}</strong>
                        <div class="file-size">${CommonUtils.formatFileSize(file.size)}</div>
                    </div>
                </div>
                <button class="remove-file" onclick="removeFile(${index})">
                    <i class="fas fa-times"></i>
                </button>
            `;
            listContainer.appendChild(fileItem);
        });
    } else {
        fileListDiv.style.display = 'none';
    }
}

// Remove a file
function removeFile(index) {
    selectedFiles.splice(index, 1);
    updateFileList();
}

// Clear all files
function clearFiles() {
    selectedFiles = [];
    document.getElementById('fileInput').value = '';
    updateFileList();
    hideResults();
    hideError();
}

// Analyze files
async function analyzeFiles() {
    if (selectedFiles.length === 0) {
        showError('Please select one file to analyze.');
        return;
    }

    const analyzeBtn = document.getElementById('analyzeBtn');
    const spinner = document.getElementById('analyzeSpinner');

    // Show loading state
    analyzeBtn.disabled = true;
    spinner.style.display = 'inline-block';
    hideError();
    hideResults();

    // Create FormData
    const formData = new FormData();
    // Send only the first file as 'file' (singular) to match API
    formData.append('file', selectedFiles[0]);

    // Add analysis options
    formData.append('includeReflection', document.getElementById('checkReflection').checked);
    formData.append('includeFields', document.getElementById('checkFields').checked);
    formData.append('includeDeadBlocks', document.getElementById('checkDeadBlocks').checked);

    try {
        const response = await fetch('/api/analyze/upload', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            displayResults(result);
            CommonUtils.showNotification('Analysis completed successfully!', 'success');
        } else {
            showError(result.error || 'Analysis failed. Please try again.');
        }
    } catch (error) {
        showError('Network error: ' + error.message);
        console.error('Analysis error:', error);
    } finally {
        // Reset button state
        analyzeBtn.disabled = false;
        spinner.style.display = 'none';
    }
}

// Display results
function displayResults(data) {
    const container = document.getElementById('resultsContainer');
    const resultsSection = document.getElementById('resultsSection');

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
                <ul class="detail-list">
                    ${Object.values(summary).map(item => `<li>${item}</li>`).join('')}
                </ul>
            </div>

            ${report.totalReflectionCalls ? `
            <div class="detail-section">
                <h4><i class="fas fa-magic"></i> Reflection Analysis</h4>
                <p>Found ${report.totalReflectionCalls} reflection calls in the codebase.</p>
            </div>
            ` : ''}
        </div>

        <div class="action-buttons">
            <button class="btn btn-primary" onclick="downloadReport('json')">
                <i class="fas fa-download"></i> Download JSON Report
            </button>
            <button class="btn btn-secondary" onclick="downloadReport('html')">
                <i class="fas fa-download"></i> Download HTML Report
            </button>
        </div>
    `;

    resultsSection.style.display = 'block';
    resultsSection.scrollIntoView({ behavior: 'smooth' });
}

// Download report
function downloadReport(format) {
    CommonUtils.showNotification(`Downloading ${format.toUpperCase()} report...`, 'info');
    // Implement download logic based on your backend
    alert(`Download ${format} report feature will be implemented with backend integration`);
}

// Error handling
function showError(message) {
    const errorAlert = document.getElementById('errorAlert');
    errorAlert.textContent = message;
    errorAlert.style.display = 'block';
    errorAlert.scrollIntoView({ behavior: 'smooth' });
}

function hideError() {
    document.getElementById('errorAlert').style.display = 'none';
}

function hideResults() {
    document.getElementById('resultsSection').style.display = 'none';
}