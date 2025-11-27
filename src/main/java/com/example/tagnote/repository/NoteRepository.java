package com.example.tagnote.repository;

import com.example.tagnote.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findAll(Pageable pageable);
    
    // Method to find all notes ordered by createdAt descending
    Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Method to find all notes ordered by createdAt ascending
    Page<Note> findAllByOrderByCreatedAtAsc(Pageable pageable);

    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames")
    List<Note> findByTagNames(@Param("tagNames") List<String> tagNames);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames ORDER BY n.createdAt DESC")
    Page<Note> findByTagNames(@Param("tagNames") List<String> tagNames, Pageable pageable);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames ORDER BY n.createdAt DESC")
    List<Note> findByTagNamesOrderByCreatedAtDesc(@Param("tagNames") List<String> tagNames);
    
    // Method to get distinct creation dates for calendar view
    @Query("SELECT DISTINCT n.createdAt FROM Note n ORDER BY n.createdAt DESC")
    List<LocalDateTime> findDistinctCreationDates();
}