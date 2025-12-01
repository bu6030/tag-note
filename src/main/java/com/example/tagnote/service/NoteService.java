package com.example.tagnote.service;

import com.example.tagnote.entity.Note;
import com.example.tagnote.entity.Tag;
import com.example.tagnote.repository.NoteRepository;
import com.example.tagnote.repository.TagRepository;
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

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Page<Note> getAllNotes(Pageable pageable) {
        return noteRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    public Note createNote(String title, String content, List<String> tagNames) {
        Note note = new Note(content);
        note.setTitle(title); // Title can be null

        if (tagNames != null) {
            for (String tagName : tagNames) {
                // Support both regular comma and Chinese comma (、) as separators
                String[] separatedTags = tagName.split("[,、]");
                for (String separatedTag : separatedTags) {
                    String trimmedTagName = separatedTag.trim();
                    if (!trimmedTagName.isEmpty()) {
                        Tag tag = tagRepository.findByName(trimmedTagName)
                            .orElseGet(() -> tagRepository.save(new Tag(trimmedTagName)));
                        note.addTag(tag);
                    }
                }
            }
        }

        return noteRepository.save(note);
    }

    public Note updateNote(Long id, String title, String content, List<String> tagNames) {
        Optional<Note> noteOptional = noteRepository.findById(id);
        if (noteOptional.isPresent()) {
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
                            Tag tag = tagRepository.findByName(trimmedTagName)
                                .orElseGet(() -> tagRepository.save(new Tag(trimmedTagName)));
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
        noteRepository.deleteById(id);
    }

    public List<Note> searchNotesByTags(List<String> tagNames) {
        return noteRepository.findByTagNamesOrderByCreatedAtDesc(tagNames);
    }

    public Page<Note> searchNotesByTags(List<String> tagNames, Pageable pageable) {
        return noteRepository.findByTagNames(tagNames, pageable);
    }

    // Method to get distinct creation dates for calendar view
    public List<LocalDateTime> getDistinctNoteDates() {
        List<LocalDateTime> dates = noteRepository.findDistinctCreationDates();
        // Extract just the date part (without time) for comparison
        return dates.stream()
            .map(date -> date.toLocalDate().atStartOfDay())
            .collect(Collectors.toList());
    }

    // Method to get total number of notes
    public long getTotalNoteCount() {
        return noteRepository.count();
    }

    // Method to get total number of tags
    public long getTotalTagCount() {
        return tagRepository.count();
    }

    // Method to get the earliest note creation date
    public LocalDateTime getFirstNoteDate() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Note> notes = noteRepository.findAllByOrderByCreatedAtAsc(pageable);
        if (notes.hasContent()) {
            return notes.getContent().get(0).getCreatedAt();
        }
        return null;
    }
}