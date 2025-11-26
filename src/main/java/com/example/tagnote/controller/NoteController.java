package com.example.tagnote.controller;

import com.example.tagnote.dto.NoteDTO;
import com.example.tagnote.entity.Note;
import com.example.tagnote.entity.Tag;
import com.example.tagnote.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private NoteService noteService;
    
    @Value("${app.notes.page-size:5}")
    private int defaultPageSize;

    @GetMapping
    public ResponseEntity<List<NoteDTO>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        List<NoteDTO> noteDTOs = notes.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(noteDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .map(note -> ResponseEntity.ok(convertToDTO(note)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NoteDTO> createNote(@RequestBody NoteDTO noteDTO) {
        Note note = noteService.createNote(noteDTO.getTitle(), noteDTO.getContent(), noteDTO.getTags());
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDTO> updateNote(@PathVariable Long id, @RequestBody NoteDTO noteDTO) {
        Note note = noteService.updateNote(id, noteDTO.getTitle(), noteDTO.getContent(), noteDTO.getTags());
        if (note != null) {
            return ResponseEntity.ok(convertToDTO(note));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginatedResponse> getAllNotesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "${app.notes.page-size:5}") int size) {
        
        // Use the configured page size if not provided or if it's invalid
        int pageSize = (size <= 0) ? defaultPageSize : Math.min(size, 100); // Cap at 100 for performance
        Pageable pageable = PageRequest.of(page, pageSize);
        
        Page<Note> notePage = noteService.getAllNotes(pageable);
        List<NoteDTO> noteDTOs = notePage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        
        PaginatedResponse response = new PaginatedResponse(
            noteDTOs,
            notePage.getNumber(),
            notePage.getSize(),
            notePage.getTotalElements(),
            notePage.getTotalPages(),
            notePage.hasNext(),
            notePage.hasPrevious()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteDTO>> searchNotes(@RequestParam(required = false) String title,
                                                    @RequestParam(required = false) List<String> tags) {
        List<Note> notes;
        if (title != null && !title.isEmpty()) {
            notes = noteService.searchNotesByTitle(title);
        } else if (tags != null && !tags.isEmpty()) {
            notes = noteService.searchNotesByTags(tags);
        } else {
            notes = noteService.getAllNotes();
        }
        
        List<NoteDTO> noteDTOs = notes.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(noteDTOs);
    }

    @GetMapping("/search/paginated")
    public ResponseEntity<PaginatedResponse> searchNotesPaginated(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "${app.notes.page-size:5}") int size) {
        
        // Use the configured page size if not provided or if it's invalid
        int pageSize = (size <= 0) ? defaultPageSize : Math.min(size, 100); // Cap at 100 for performance
        Pageable pageable = PageRequest.of(page, pageSize);
        
        Page<Note> notePage;
        if (title != null && !title.isEmpty()) {
            notePage = noteService.searchNotesByTitle(title, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            notePage = noteService.searchNotesByTags(tags, pageable);
        } else {
            notePage = noteService.getAllNotes(pageable);
        }
        
        List<NoteDTO> noteDTOs = notePage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        
        PaginatedResponse response = new PaginatedResponse(
            noteDTOs,
            notePage.getNumber(),
            notePage.getSize(),
            notePage.getTotalElements(),
            notePage.getTotalPages(),
            notePage.hasNext(),
            notePage.hasPrevious()
        );
        
        return ResponseEntity.ok(response);
    }

    private NoteDTO convertToDTO(Note note) {
        List<String> tagNames = note.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
                
        return new NoteDTO(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                tagNames
        );
    }

    // Inner class for paginated response
    public static class PaginatedResponse {
        private List<NoteDTO> content;
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public PaginatedResponse(List<NoteDTO> content, int currentPage, int pageSize, 
                               long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            this.content = content;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        // Getters and setters
        public List<NoteDTO> getContent() { return content; }
        public void setContent(List<NoteDTO> content) { this.content = content; }
        
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        
        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
        
        public boolean isHasPrevious() { return hasPrevious; }
        public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    }
}