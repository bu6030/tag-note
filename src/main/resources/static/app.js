// API base URL
const API_BASE = '/api';

// DOM Elements
const noteForm = document.getElementById('noteForm');
const notesContainer = document.getElementById('notesContainer');
const paginationControls = document.getElementById('paginationControls');
const tagSearchInput = document.getElementById('tagSearchInput');
const searchBtn = document.getElementById('searchBtn');
const clearBtn = document.getElementById('clearBtn');

// Rich text editor elements
const contentEditor = document.getElementById('content');
const boldBtn = document.getElementById('boldBtn');
const numberedListBtn = document.getElementById('numberedListBtn');
const bulletListBtn = document.getElementById('bulletListBtn');

// Pagination state
let currentPage = 0;
let currentTagSearchTerm = '';
let pageSize = 5; // Increased page size for better UX

// Add a variable to track if we're editing a note
let editingNoteId = null;

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    loadNotesPaginated();
    
    // Add event listeners for rich text editor buttons
    if (boldBtn) {
        boldBtn.addEventListener('click', () => {
            document.execCommand('bold', false, null);
            contentEditor.focus();
        });
    }
    
    if (numberedListBtn) {
        numberedListBtn.addEventListener('click', () => {
            document.execCommand('insertOrderedList', false, null);
            contentEditor.focus();
        });
    }
    
    if (bulletListBtn) {
        bulletListBtn.addEventListener('click', () => {
            document.execCommand('insertUnorderedList', false, null);
            contentEditor.focus();
        });
    }
});

noteForm.addEventListener('submit', handleNoteSubmit);
searchBtn.addEventListener('click', handleSearch);
clearBtn.addEventListener('click', clearSearch);

// Add event listeners for Enter key in search inputs
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

// Simple HTML sanitizer to prevent XSS while allowing safe formatting
function sanitizeHtml(html) {
    if (!html) return '';
    
    // Create a temporary element to parse HTML
    const temp = document.createElement('div');
    temp.innerHTML = html;
    
    // Remove all elements except allowed ones
    const allowedTags = ['B', 'STRONG', 'OL', 'UL', 'LI', 'BR'];
    const elements = temp.querySelectorAll('*');
    
    for (let i = elements.length - 1; i >= 0; i--) {
        const element = elements[i];
        if (!allowedTags.includes(element.tagName)) {
            // Replace unauthorized elements with their text content
            element.outerHTML = element.innerHTML;
        }
    }
    
    return temp.innerHTML;
}

// Render notes to the page
function renderNotes(notes) {
    if (!notes || notes.length === 0) {
        notesContainer.innerHTML = '<p class="no-notes">No notes found. Create your first note above!</p>';
        return;
    }

    notesContainer.innerHTML = notes.map(note => `
        <div class="note-card" data-id="${note.id}">
            <div class="note-content">${sanitizeHtml(note.content)}</div>
            <div class="note-meta">
                <span>Created: ${formatDate(note.createdAt)}</span>
                <span>Updated: ${formatDate(note.updatedAt)}</span>
            </div>
            ${note.tags && note.tags.length > 0 ? `
            <div class="note-tags">
                ${note.tags.map(tag => `<span class="tag">${escapeHtml(tag)}</span>`).join('')}
            </div>` : ''}
            <div class="note-actions">
                <button class="btn-edit" onclick="editNote(${note.id})">修改</button>
                <button class="btn-delete" onclick="deleteNote(${note.id})">删除</button>
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
    if (currentTagSearchTerm) {
        searchNotesPaginated(page);
    } else {
        loadNotesPaginated(page);
    }
}

// Handle note form submission
async function handleNoteSubmit(event) {
    event.preventDefault();
    
    const content = contentEditor.innerHTML; // Get HTML content instead of plain text
    const tagsInput = document.getElementById('tags').value;
    
    // Split tags by both regular comma and Chinese comma (、)
    const tags = tagsInput.split(/[,、]/)
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0);
    
    const noteData = {
        title: "", // Empty title to satisfy database constraint
        content: content,
        tags: tags
    };
    
    try {
        showLoading(true);
        let response;
        
        if (editingNoteId) {
            // Update existing note
            response = await fetch(`${API_BASE}/notes/${editingNoteId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(noteData)
            });
        } else {
            // Create new note
            response = await fetch(`${API_BASE}/notes`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(noteData)
            });
        }
        
        if (response.ok) {
            // Clear form and reset editing state
            noteForm.reset();
            contentEditor.innerHTML = ''; // Clear rich text editor
            editingNoteId = null;
            document.querySelector('.note-form-section h2').textContent = '添加新笔记'; // Reset header
            
            // Reload notes (go back to first page)
            currentPage = 0;
            if (currentTagSearchTerm) {
                searchNotesPaginated(currentPage);
            } else {
                loadNotesPaginated(currentPage);
            }
            
            showSuccess(editingNoteId ? 'Note updated successfully!' : 'Note saved successfully!');
        } else {
            throw new Error(editingNoteId ? 'Failed to update note' : 'Failed to save note');
        }
        showLoading(false);
    } catch (error) {
        console.error('Error saving/updating note:', error);
        showError(editingNoteId ? 'Failed to update note' : 'Failed to save note');
        showLoading(false);
    }
}

// Edit note - load note data into the form
async function editNote(id) {
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/notes/${id}`);
        
        if (response.ok) {
            const note = await response.json();
            
            // Populate form with note data
            // For editing, we want to show the raw HTML in the editor
            contentEditor.innerHTML = note.content || '';
            
            document.getElementById('tags').value = note.tags ? note.tags.join('、') : ''; // Use Chinese comma as separator
            
            // Set editing state
            editingNoteId = id;
            
            // Update form header to indicate editing
            document.querySelector('.note-form-section h2').textContent = '编辑笔记';
            
            // Scroll to form
            document.querySelector('.note-form-section').scrollIntoView({ behavior: 'smooth' });
            
            showLoading(false);
        } else {
            throw new Error('Failed to load note for editing');
        }
    } catch (error) {
        console.error('Error loading note for editing:', error);
        showError('Failed to load note for editing');
        showLoading(false);
    }
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
            if (currentTagSearchTerm) {
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
        if (currentTagSearchTerm) {
            // Search by tags only
            // Split tags by both regular comma and Chinese comma (、)
            const tags = currentTagSearchTerm.split(/[,、]/).map(tag => tag.trim()).filter(tag => tag.length > 0);
            const tagsParam = tags.join(',');
            url = `${API_BASE}/notes/search/paginated?tags=${encodeURIComponent(tagsParam)}&page=${page}&size=${pageSize}`;
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
    currentTagSearchTerm = tagSearchInput.value.trim();
    currentPage = 0; // Reset to first page when searching
    searchNotesPaginated(currentPage);
}

// Clear search
function clearSearch() {
    tagSearchInput.value = '';
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