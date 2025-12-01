package com.example.tagnote.service;

import com.example.tagnote.entity.Tag;
import com.example.tagnote.repository.TagRepository;
import com.example.tagnote.service.UserService;
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

    @Autowired
    private UserService userService;

    public List<Tag> getAllTags() {
        String username = userService.getUsername();
        return tagRepository.findByUsername(username);
    }

    public Optional<Tag> getTagById(Long id) {
        String username = userService.getUsername();
        return tagRepository.findById(id).filter(tag -> tag.getUsername().equals(username));
    }

    public Tag saveTag(Tag tag) {
        String username = userService.getUsername();
        tag.setUsername(username);
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        String username = userService.getUsername();
        // Get the tag to find all notes associated with it
        tagRepository.findById(id).ifPresent(tag -> {
            if (tag.getUsername().equals(username)) {
                // Delete all notes associated with this tag
                tag.getNotes().forEach(note -> noteService.deleteNote(note.getId()));
                // Delete the tag itself
                tagRepository.deleteById(id);
            }
        });
    }

    public Optional<Tag> getTagByName(String name) {
        String username = userService.getUsername();
        return tagRepository.findByNameAndUsername(name, username);
    }
}