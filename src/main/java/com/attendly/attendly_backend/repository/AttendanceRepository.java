package com.attendly.attendly_backend.repository;

import com.attendly.attendly_backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findBySessionId(Long sessionId);
}