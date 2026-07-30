package com.attendly.attendly_backend.repository;

import com.attendly.attendly_backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}