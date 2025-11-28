package com.example.tagnote.service;

import com.example.tagnote.entity.Tag;
import com.example.tagnote.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NoteService noteService;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        // Get the tag to find all notes associated with it
        tagRepository.findById(id).ifPresent(tag -> {
            // Delete all notes associated with this tag
            tag.getNotes().forEach(note -> noteService.deleteNote(note.getId()));
            // Delete the tag itself
            tagRepository.deleteById(id);
        });
    }

    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }
}