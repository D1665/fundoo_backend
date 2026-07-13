package com.fundoonotes.fundoo_notes.jms;

import com.fundoonotes.fundoo_notes.model.Note;
import com.fundoonotes.fundoo_notes.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ReminderProducer reminderProducer;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkReminders() {

        System.out.println("Reminders check: " + LocalDateTime.now());

        List<Note> dueNotes = noteRepository
                .findByReminderBeforeAndIsTrashedFalse(
                        LocalDateTime.now()
                );

        if (dueNotes.isEmpty()) {
            System.out.println("No reminders.");
            return;
        }

        System.out.println("Found " + dueNotes.size()
                + " reminder(s) due!");

        for (Note note : dueNotes) {
            reminderProducer.sendReminder(
                    note.getUser().getEmail(),
                    note.getTitle()
            );
            note.setReminder(null);
            noteRepository.save(note);
            System.out.println("Reminder processed: "
                    + note.getTitle());
        }
    }
}