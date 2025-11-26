package com.example.tagnote.repository;

import com.example.tagnote.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTitleContainingIgnoreCase(String title);
        
        Page<Note> findAll(Pageable pageable);
        Page<Note> findByTitleContainingIgnoreCase(String title, Pageable pageable);
        
        // Method to find all notes ordered by createdAt descending
        Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames")
    List<Note> findByTagNames(@Param("tagNames") List<String> tagNames);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames ORDER BY n.createdAt DESC")
    Page<Note> findByTagNames(@Param("tagNames") List<String> tagNames, Pageable pageable);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames ORDER BY n.createdAt DESC")
    List<Note> findByTagNamesOrderByCreatedAtDesc(@Param("tagNames") List<String> tagNames);
    
    // Search methods with ordering by createdAt descending
    List<Note> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);
    Page<Note> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);
}