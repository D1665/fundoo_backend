package com.fundoonotes.fundoo_notes.repository;

import com.fundoonotes.fundoo_notes.model.Note;
import com.fundoonotes.fundoo_notes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Active notes
    List<Note> findByUserAndIsTrashedFalseAndIsArchivedFalse(User user);

    // Archived notes
    List<Note> findByUserAndIsArchivedTrueAndIsTrashedFalse(User user);

    // Trashed notes
    List<Note> findByUserAndIsTrashedTrue(User user);

    // Pinned notes
    List<Note> findByUserAndIsPinnedTrueAndIsTrashedFalse(User user);

    // Find note by id and user
    Optional<Note> findByIdAndUser(Long id, User user);

    // Search by title or content — clean name using @Query
    @Query("SELECT n FROM Note n WHERE n.user = :user " +
            "AND n.isTrashed = false " +
            "AND n.isArchived = false " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Note> searchNotes(@Param("user") User user,
                           @Param("keyword") String keyword);

    // Filter by color
    List<Note> findByUserAndColorAndIsTrashedFalseAndIsArchivedFalse(
            User user, String color);

    // Get notes by label
    List<Note> findByUserAndLabels_IdAndIsTrashedFalse(
            User user, Long labelId);

    // Find notes where reminder time has passed
    List<Note> findByReminderBeforeAndIsTrashedFalse(
            LocalDateTime reminderTime);
}