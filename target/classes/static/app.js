// API base URL
const API_BASE = '/api';

// DOM Elements
const noteForm = document.getElementById('noteForm');
const notesContainer = document.getElementById('notesContainer');
const paginationControls = document.getElementById('paginationControls');
const searchInput = document.getElementById('searchInput');
const tagSearchInput = document.getElementById('tagSearchInput');
const searchBtn = document.getElementById('searchBtn');
const clearBtn = document.getElementById('clearBtn');

// Pagination state
let currentPage = 0;
let currentSearchTerm = '';
let currentTagSearchTerm = '';
let pageSize = 5; // Increased page size for better UX

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    loadNotesPaginated();
});

noteForm.addEventListener('submit', handleNoteSubmit);
searchBtn.addEventListener('click', handleSearch);
clearBtn.addEventListener('click', clearSearch);

// Add event listeners for Enter key in search inputs
searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        handleSearch();
    }
});

tagSearchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        handleSearch();
    }
});

// Load all notes with pagination
async function loadNotesPaginated(page = 0) {
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/notes/paginated?page=${page}&size=${pageSize}`);
        const paginatedResponse = await response.json();
        renderNotes(paginatedResponse.content);
        renderPaginationControls(paginatedResponse, page);
        showLoading(false);
    } catch (error) {
        console.error('Error loading notes:', error);
        showError('Failed to load notes');
        showLoading(false);
    }
}

// Render notes to the page
function renderNotes(notes) {
    if (!notes || notes.length === 0) {
        notesContainer.innerHTML = '<p class="no-notes">No notes found. Create your first note above!</p>';
        return;
    }

    notesContainer.innerHTML = notes.map(note => `
        <div class="note-card" data-id="${note.id}">
            <h3 class="note-title">${escapeHtml(note.title)}</h3>
            <div class="note-content">${escapeHtml(note.content)}</div>
            <div class="note-meta">
                <span>Created: ${formatDate(note.createdAt)}</span>
                <span>Updated: ${formatDate(note.updatedAt)}</span>
            </div>
            ${note.tags && note.tags.length > 0 ? `
            <div class="note-tags">
                ${note.tags.map(tag => `<span class="tag">${escapeHtml(tag)}</span>`).join('')}
            </div>` : ''}
            <div class="note-actions">
                <button class="btn-edit" onclick="editNote(${note.id})">Edit</button>
                <button class="btn-delete" onclick="deleteNote(${note.id})">Delete</button>
            </div>
        </div>
    `).join('');
}

// Render pagination controls
function renderPaginationControls(paginatedResponse, currentPage) {
    const { totalPages, hasNext, hasPrevious, totalElements } = paginatedResponse;
    
    if (totalPages <= 1) {
        paginationControls.innerHTML = '';
        return;
    }
    
    let paginationHTML = `
        <div class="pagination-info">
            Showing ${currentPage * pageSize + 1} to ${Math.min((currentPage + 1) * pageSize, totalElements)} of ${totalElements} notes
        </div>
        <button id="firstPageBtn" ${hasPrevious ? '' : 'disabled'}>&laquo; First</button>
        <button id="prevPageBtn" ${hasPrevious ? '' : 'disabled'}>Previous</button>
        <span>Page ${currentPage + 1} of ${totalPages}</span>
        <button id="nextPageBtn" ${hasNext ? '' : 'disabled'}>Next</button>
        <button id="lastPageBtn" ${hasNext ? '' : 'disabled'}>Last &raquo;</button>
    `;
    
    paginationControls.innerHTML = paginationHTML;
    
    // Add event listeners to pagination buttons
    if (hasPrevious) {
        document.getElementById('firstPageBtn').addEventListener('click', () => goToPage(0));
        document.getElementById('prevPageBtn').addEventListener('click', () => goToPage(currentPage - 1));
    }
    
    if (hasNext) {
        document.getElementById('nextPageBtn').addEventListener('click', () => goToPage(currentPage + 1));
        document.getElementById('lastPageBtn').addEventListener('click', () => goToPage(totalPages - 1));
    }
}

// Go to specific page
function goToPage(page) {
    if (currentSearchTerm || currentTagSearchTerm) {
        searchNotesPaginated(page);
    } else {
        loadNotesPaginated(page);
    }
}

// Handle note form submission
async function handleNoteSubmit(event) {
    event.preventDefault();
    
    const title = document.getElementById('title').value;
    const content = document.getElementById('content').value;
    const tagsInput = document.getElementById('tags').value;
    
    // Split tags by both regular comma and Chinese comma (、)
    const tags = tagsInput.split(/[,、]/)
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0);
    
    const noteData = {
        title: title,
        content: content,
        tags: tags
    };
    
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/notes`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(noteData)
        });
        
        if (response.ok) {
            // Clear form
            noteForm.reset();
            
            // Reload notes (go back to first page)
            currentPage = 0;
            if (currentSearchTerm || currentTagSearchTerm) {
                searchNotesPaginated(currentPage);
            } else {
                loadNotesPaginated(currentPage);
            }
            
            showSuccess('Note saved successfully!');
        } else {
            throw new Error('Failed to save note');
        }
        showLoading(false);
    } catch (error) {
        console.error('Error saving note:', error);
        showError('Failed to save note');
        showLoading(false);
    }
}

// Edit note (stub implementation)
function editNote(id) {
    alert(`Edit functionality for note ${id} would be implemented here.`);
    // In a full implementation, this would load the note data into the form
}

// Delete note
async function deleteNote(id) {
    if (!confirm('Are you sure you want to delete this note?')) {
        return;
    }
    
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/notes/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            // Reload notes
            if (currentSearchTerm || currentTagSearchTerm) {
                searchNotesPaginated(currentPage);
            } else {
                loadNotesPaginated(currentPage);
            }
            
            showSuccess('Note deleted successfully!');
        } else {
            throw new Error('Failed to delete note');
        }
        showLoading(false);
    } catch (error) {
        console.error('Error deleting note:', error);
        showError('Failed to delete note');
        showLoading(false);
    }
}

// Handle search with pagination
async function searchNotesPaginated(page = 0) {
    try {
        showLoading(true);
        let url;
        if (currentSearchTerm && !currentTagSearchTerm) {
            // Search by title only
            url = `${API_BASE}/notes/search/paginated?title=${encodeURIComponent(currentSearchTerm)}&page=${page}&size=${pageSize}`;
        } else if (!currentSearchTerm && currentTagSearchTerm) {
            // Search by tags only
            // Split tags by both regular comma and Chinese comma (、)
            const tags = currentTagSearchTerm.split(/[,、]/).map(tag => tag.trim()).filter(tag => tag.length > 0);
            const tagsParam = tags.join(',');
            url = `${API_BASE}/notes/search/paginated?tags=${encodeURIComponent(tagsParam)}&page=${page}&size=${pageSize}`;
        } else if (currentSearchTerm && currentTagSearchTerm) {
            // Search by both title and tags - for simplicity, we'll search by title
            url = `${API_BASE}/notes/search/paginated?title=${encodeURIComponent(currentSearchTerm)}&page=${page}&size=${pageSize}`;
        } else {
            // Load all notes
            url = `${API_BASE}/notes/paginated?page=${page}&size=${pageSize}`;
        }
        
        const response = await fetch(url);
        const paginatedResponse = await response.json();
        renderNotes(paginatedResponse.content);
        renderPaginationControls(paginatedResponse, page);
        showLoading(false);
    } catch (error) {
        console.error('Error searching notes:', error);
        showError('Failed to search notes');
        showLoading(false);
    }
}

// Handle search
async function handleSearch() {
    currentSearchTerm = searchInput.value.trim();
    currentTagSearchTerm = tagSearchInput.value.trim();
    currentPage = 0; // Reset to first page when searching
    searchNotesPaginated(currentPage);
}

// Clear search
function clearSearch() {
    searchInput.value = '';
    tagSearchInput.value = '';
    currentSearchTerm = '';
    currentTagSearchTerm = '';
    currentPage = 0; // Reset to first page
    loadNotesPaginated(currentPage);
}

// Utility function to escape HTML
function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Utility function to format date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString();
}

// Show loading indicator
function showLoading(show) {
    // In a real app, you might show a loading spinner
    if (show) {
        console.log('Loading...');
    }
}

// Show success message
function showSuccess(message) {
    // In a real app, you might show a toast notification
    console.log('Success:', message);
    // Create a temporary alert with the new color scheme
    const alert = document.createElement('div');
    alert.textContent = message;
    alert.style.position = 'fixed';
    alert.style.top = '20px';
    alert.style.right = '20px';
    alert.style.backgroundColor = '#30cf79';
    alert.style.color = 'white';
    alert.style.padding = '15px 20px';
    alert.style.borderRadius = '6px';
    alert.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    alert.style.zIndex = '1000';
    alert.style.fontWeight = '500';
    document.body.appendChild(alert);
    
    // Remove the alert after 3 seconds
    setTimeout(() => {
        if (alert.parentNode) {
            alert.parentNode.removeChild(alert);
        }
    }, 3000);
}

// Show error message
function showError(message) {
    // In a real app, you might show a toast notification
    console.error('Error:', message);
    alert('Error: ' + message);
}