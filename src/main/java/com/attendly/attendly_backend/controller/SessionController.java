package com.attendly.attendly_backend.controller;

import com.attendly.attendly_backend.entity.Session;
import com.attendly.attendly_backend.repository.SessionRepository;
import com.attendly.attendly_backend.service.TokenService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionRepository sessionRepository;
    private final TokenService tokenService;

    public SessionController(SessionRepository sessionRepository, TokenService tokenService) {
        this.sessionRepository = sessionRepository;
        this.tokenService = tokenService;
    }

    // Teacher starts a new class session
    @PostMapping("/start")
    public Session startSession(@RequestBody Map<String, Object> body) {
        Session session = new Session();
        session.setClassName((String) body.get("className"));
        session.setLatitude(Double.valueOf(body.get("latitude").toString()));
        session.setLongitude(Double.valueOf(body.get("longitude").toString()));
        session.setRadiusMeters(Double.valueOf(body.get("radiusMeters").toString()));
        session.setActive(true);
        session.setCreatedAt(LocalDateTime.now());

        // Set a temporary placeholder token to satisfy the not-null constraint
        session.setCurrentToken("PENDING");
        session.setTokenExpiry(LocalDateTime.now().plusSeconds(tokenService.getTokenValiditySeconds()));

        // First save to get the auto-generated ID
        Session saved = sessionRepository.save(session);

        // Now generate the real token using that ID, and update
        String token = tokenService.getRandomInitialToken(saved.getId());
        saved.setCurrentToken(token);
        saved.setTokenExpiry(LocalDateTime.now().plusSeconds(tokenService.getTokenValiditySeconds()));

        return sessionRepository.save(saved);
    }

    // Get the current live token for a session (this is what becomes the QR code)
    @GetMapping("/{id}/current-token")
    public Map<String, Object> getCurrentToken(@PathVariable Long id) {
        Optional<Session> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isEmpty()) {
            return Map.of("error", "Session not found");
        }
        Session session = sessionOpt.get();
        return Map.of(
                "sessionId", session.getId(),
                "token", session.getCurrentToken(),
                "expiresAt", session.getTokenExpiry()
        );
    }

    // Teacher ends the session
    @PostMapping("/{id}/stop")
    public Map<String, String> stopSession(@PathVariable Long id) {
        Optional<Session> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isEmpty()) {
            return Map.of("error", "Session not found");
        }
        Session session = sessionOpt.get();
        session.setActive(false);
        sessionRepository.save(session);
        return Map.of("message", "Session stopped");
    }
}