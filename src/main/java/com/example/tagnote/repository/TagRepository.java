package com.example.tagnote.repository;

import com.example.tagnote.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    Optional<Tag> findByNameAndUsername(String name, String username);

    // Method to find all tags by username
    java.util.List<Tag> findByUsername(String username);
}