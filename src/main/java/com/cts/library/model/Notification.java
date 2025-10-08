package com.cts.library.model;

import jakarta.persistence.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(
	    name = "notifications",
	    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "book_id"})
	)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private String message;

    private Date dateSent;

    @ManyToOne
    @JoinColumn(name="member_id")
    @JsonBackReference
    private Member member;

    @ManyToOne
    @JoinColumn(name="book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name="fine_id")
    private Fine fine;

    private long overdueDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    private NotificationStage stage;

    public Notification() {}

    public Notification(long overdueDays, Member member, Book book, Fine fine, String message, Date dateSent, NotificationStage stage) {
        this.overdueDays = overdueDays;
        this.member = member;
        this.book = book;
        this.fine = fine;
        this.message = message;
        this.dateSent = dateSent;
        this.stage = stage;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Fine getFine() {
        return fine;
    }

    public void setFine(Fine fine) {
        this.fine = fine;
    }

    public long getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(long overdueDays) {
        this.overdueDays = overdueDays;
    }

    public NotificationStage getStage() {
        return stage;
    }

    public void setStage(NotificationStage stage) {
        this.stage = stage;
    }
}
