package com.fundoonotes.fundoo_notes.repository;

import com.fundoonotes.fundoo_notes.model.Label;
import com.fundoonotes.fundoo_notes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    // Get all labels of a user
    List<Label> findByUser(User user);

    // Find label by id and user (security check)
    Optional<Label> findByIdAndUser(Long id, User user);

    // Check if label name already exists for user
    boolean existsByNameAndUser(String name, User user);
}