package com.attendly.attendly_backend.service;

import com.attendly.attendly_backend.entity.Session;
import com.attendly.attendly_backend.repository.SessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class TokenService {

    private final SessionRepository sessionRepository;
    private static final String SECRET = "AttendlyTokenRotationSecretKey987!";
    private static final int TOKEN_VALID_SECONDS = 60; // how often the QR refreshes

    public TokenService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // Generates a fresh token for a given session using HMAC-SHA256
    public String generateToken(Long sessionId) {
        try {
            long timeWindow = System.currentTimeMillis() / (TOKEN_VALID_SECONDS * 1000);
            String data = sessionId + ":" + timeWindow;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes());

            // Shorten it for a cleaner QR code (first 16 chars of the hash, URL-safe)
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return token.substring(0, 16);
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    // Runs automatically every TOKEN_VALID_SECONDS to refresh tokens for all active sessions
    @Scheduled(fixedRate = TOKEN_VALID_SECONDS * 1000)
    public void rotateActiveSessionTokens() {
        List<Session> activeSessions = sessionRepository.findAll()
                .stream()
                .filter(Session::isActive)
                .toList();

        for (Session session : activeSessions) {
            String newToken = generateToken(session.getId());
            session.setCurrentToken(newToken);
            session.setTokenExpiry(LocalDateTime.now().plusSeconds(TOKEN_VALID_SECONDS));
            sessionRepository.save(session);
        }
    }

    public String getRandomInitialToken(Long sessionId) {
        return generateToken(sessionId);
    }

    // Exposes the rotation interval so other classes (like SessionController)
    // can stay in sync instead of hardcoding a separate number
    public int getTokenValiditySeconds() {
        return TOKEN_VALID_SECONDS;
    }
}