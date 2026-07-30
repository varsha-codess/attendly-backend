package com.attendly.attendly_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    @Column(nullable = false)
    private String studentUsername;

    @Column(nullable = false)
    private Double studentLatitude;

    @Column(nullable = false)
    private Double studentLongitude;

    @Column(nullable = false)
    private String status; // "PRESENT" or "REJECTED"

    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public Double getStudentLatitude() { return studentLatitude; }
    public void setStudentLatitude(Double studentLatitude) { this.studentLatitude = studentLatitude; }

    public Double getStudentLongitude() { return studentLongitude; }
    public void setStudentLongitude(Double studentLongitude) { this.studentLongitude = studentLongitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}