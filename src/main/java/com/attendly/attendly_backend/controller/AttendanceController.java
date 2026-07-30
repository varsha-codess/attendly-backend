package com.attendly.attendly_backend.controller;

import com.attendly.attendly_backend.entity.Attendance;
import com.attendly.attendly_backend.entity.Session;
import com.attendly.attendly_backend.repository.AttendanceRepository;
import com.attendly.attendly_backend.repository.SessionRepository;
import com.attendly.attendly_backend.service.GeoService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final GeoService geoService;

    public AttendanceController(AttendanceRepository attendanceRepository,
                                SessionRepository sessionRepository,
                                GeoService geoService) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.geoService = geoService;
    }

    @PostMapping("/mark")
    public Map<String, Object> markAttendance(@RequestBody Map<String, Object> body) {
        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String scannedToken = (String) body.get("token");
        String studentUsername = (String) body.get("studentUsername");
        double studentLat = Double.parseDouble(body.get("latitude").toString());
        double studentLon = Double.parseDouble(body.get("longitude").toString());

        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return reject(sessionId, studentUsername, studentLat, studentLon, "Session not found");
        }

        Session session = sessionOpt.get();

        // Check 1: Is the session still active?
        if (!session.isActive()) {
            return reject(sessionId, studentUsername, studentLat, studentLon, "Session is not active");
        }

        // Check 2: Does the scanned token match the current valid token?
        if (!session.getCurrentToken().equals(scannedToken)) {
            return reject(sessionId, studentUsername, studentLat, studentLon, "Invalid or expired QR token");
        }

        // Check 3: Has this token expired (extra safety on top of the match check)?
        if (LocalDateTime.now().isAfter(session.getTokenExpiry())) {
            return reject(sessionId, studentUsername, studentLat, studentLon, "QR token expired");
        }

        // Check 4: Is the student within the geofence radius?
        boolean withinRange = geoService.isWithinRadius(
                session.getLatitude(), session.getLongitude(),
                studentLat, studentLon,
                session.getRadiusMeters()
        );
        if (!withinRange) {
            double distance = geoService.calculateDistanceMeters(
                    session.getLatitude(), session.getLongitude(), studentLat, studentLon);
            return reject(sessionId, studentUsername, studentLat, studentLon,
                    "Outside geofence range (distance: " + Math.round(distance) + "m)");
        }

        // Check 5: Has this student already marked attendance for this session?
        List<Attendance> existing = attendanceRepository.findBySessionId(sessionId);
        boolean alreadyMarked = existing.stream()
                .anyMatch(a -> a.getStudentUsername().equals(studentUsername)
                        && a.getStatus().equals("PRESENT"));
        if (alreadyMarked) {
            return reject(sessionId, studentUsername, studentLat, studentLon,
                    "Attendance already marked for this session");
        }

        // All checks passed — mark PRESENT
        Attendance attendance = new Attendance();
        attendance.setSessionId(sessionId);
        attendance.setStudentUsername(studentUsername);
        attendance.setStudentLatitude(studentLat);
        attendance.setStudentLongitude(studentLon);
        attendance.setStatus("PRESENT");
        attendance.setTimestamp(LocalDateTime.now());
        attendanceRepository.save(attendance);

        return Map.of(
                "status", "PRESENT",
                "message", "Attendance marked successfully",
                "sessionId", sessionId,
                "studentUsername", studentUsername
        );
    }

    private Map<String, Object> reject(Long sessionId, String studentUsername,
                                       double lat, double lon, String reason) {
        Attendance attendance = new Attendance();
        attendance.setSessionId(sessionId);
        attendance.setStudentUsername(studentUsername);
        attendance.setStudentLatitude(lat);
        attendance.setStudentLongitude(lon);
        attendance.setStatus("REJECTED");
        attendance.setRejectionReason(reason);
        attendance.setTimestamp(LocalDateTime.now());
        attendanceRepository.save(attendance);

        return Map.of(
                "status", "REJECTED",
                "reason", reason,
                "sessionId", sessionId,
                "studentUsername", studentUsername
        );
    }

    // Teacher view: see all attendance records for a session
    @GetMapping("/session/{sessionId}")
    public List<Attendance> getAttendanceForSession(@PathVariable Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId);
    }
}