package com.example.tagnote.service;

import com.example.tagnote.entity.Note;
import com.example.tagnote.entity.Tag;
import com.example.tagnote.repository.NoteRepository;
import com.example.tagnote.repository.TagRepository;
import com.example.tagnote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserService userService;

    public List<Note> getAllNotes() {
        String username = userService.getUsername();
        Pageable pageable = PageRequest.of(0, 1000); // Get all notes, capped at 1000
        return noteRepository.findByUsernameOrderByCreatedAtDesc(username, pageable).getContent();
    }

    public Page<Note> getAllNotes(Pageable pageable) {
        String username = userService.getUsername();
        return noteRepository.findByUsernameOrderByCreatedAtDesc(username, pageable);
    }

    public Optional<Note> getNoteById(Long id) {
        String username = userService.getUsername();
        return noteRepository.findById(id).filter(note -> note.getUsername().equals(username));
    }

    public Note saveNote(Note note) {
        String username = userService.getUsername();
        note.setUsername(username);
        return noteRepository.save(note);
    }

    public Note createNote(String title, String content, List<String> tagNames) {
        String username = userService.getUsername();
        Note note = new Note(content, username);
        note.setTitle(title); // Title can be null

        if (tagNames != null) {
            for (String tagName : tagNames) {
                // Support both regular comma and Chinese comma (、) as separators
                String[] separatedTags = tagName.split("[,、]");
                for (String separatedTag : separatedTags) {
                    String trimmedTagName = separatedTag.trim();
                    if (!trimmedTagName.isEmpty()) {
                        Tag tag = tagRepository.findByNameAndUsername(trimmedTagName, username)
                            .orElseGet(() -> {
                                Tag newTag = new Tag(trimmedTagName, username);
                                return tagRepository.save(newTag);
                            });
                        note.addTag(tag);
                    }
                }
            }
        }

        return noteRepository.save(note);
    }

    public Note updateNote(Long id, String title, String content, List<String> tagNames) {
        String username = userService.getUsername();
        Optional<Note> noteOptional = noteRepository.findById(id);
        if (noteOptional.isPresent() && noteOptional.get().getUsername().equals(username)) {
            Note note = noteOptional.get();
            note.setTitle(title); // Title can be null
            note.setContent(content);

            // Clear existing tags
            note.getTags().clear();

            // Add new tags
            if (tagNames != null) {
                for (String tagName : tagNames) {
                    // Support both regular comma and Chinese comma (、) as separators
                    String[] separatedTags = tagName.split("[,、]");
                    for (String separatedTag : separatedTags) {
                        String trimmedTagName = separatedTag.trim();
                        if (!trimmedTagName.isEmpty()) {
                            Tag tag = tagRepository.findByNameAndUsername(trimmedTagName, username)
                                .orElseGet(() -> {
                                    Tag newTag = new Tag(trimmedTagName, username);
                                    return tagRepository.save(newTag);
                                });
                            note.addTag(tag);
                        }
                    }
                }
            }

            return noteRepository.save(note);
        }
        return null;
    }

    public void deleteNote(Long id) {
        String username = userService.getUsername();
        noteRepository.findById(id).ifPresent(note -> {
            if (note.getUsername().equals(username)) {
                noteRepository.deleteById(id);
            }
        });
    }

    public List<Note> searchNotesByTags(List<String> tagNames) {
        String username = userService.getUsername();
        return noteRepository.findByTagNamesAndUsernameOrderByCreatedAtDesc(tagNames, username);
    }

    public Page<Note> searchNotesByTags(List<String> tagNames, Pageable pageable) {
        String username = userService.getUsername();
        return noteRepository.findByTagNamesAndUsername(tagNames, username, pageable);
    }

    // Method to get distinct creation dates for calendar view
    public List<LocalDateTime> getDistinctNoteDates() {
        String username = userService.getUsername();
        List<LocalDateTime> dates = noteRepository.findDistinctCreationDatesByUsername(username);
        // Extract just the date part (without time) for comparison
        return dates.stream()
            .map(date -> date.toLocalDate().atStartOfDay())
            .collect(Collectors.toList());
    }

    // Method to get total number of notes
    public long getTotalNoteCount() {
        String username = userService.getUsername();
        Pageable pageable = PageRequest.of(0, 1); // We only need the count, so page size 1 is enough
        return noteRepository.findByUsername(username, pageable).getTotalElements();
    }

    // Method to get total number of tags
    public long getTotalTagCount() {
        String username = userService.getUsername();
        return tagRepository.findByUsername(username).size();
    }

    // Method to get the earliest note creation date
    public LocalDateTime getFirstNoteDate() {
        String username = userService.getUsername();
        Pageable pageable = PageRequest.of(0, 1);
        Page<Note> notes = noteRepository.findByUsername(username, pageable);
        if (notes.hasContent()) {
            return notes.getContent().get(0).getCreatedAt();
        }
        return null;
    }
}