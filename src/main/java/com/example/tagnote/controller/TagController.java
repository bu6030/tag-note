package com.example.tagnote.controller;

import com.example.tagnote.dto.TagDTO;
import com.example.tagnote.entity.Tag;
import com.example.tagnote.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagDTO> tagDTOs = tags.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id)
            .map(tag -> ResponseEntity.ok(convertToDTO(tag)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        Tag tag = new Tag(tagDTO.getName());
        Tag savedTag = tagService.saveTag(tag);
        return ResponseEntity.ok(convertToDTO(savedTag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    private TagDTO convertToDTO(Tag tag) {
        return new TagDTO(
            tag.getId(),
            tag.getName(),
            tag.getCreatedAt());
    }
}