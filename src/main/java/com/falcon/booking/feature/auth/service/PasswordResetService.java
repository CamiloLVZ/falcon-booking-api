package com.falcon.booking.feature.auth.service;

import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.feature.auth.exception.InvalidPasswordResetTokenException;
import com.falcon.booking.persistence.entity.PasswordResetTokenEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.PasswordResetTokenRepository;
import com.falcon.booking.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;

    @Value("${app.password-reset.token-expiration-minutes}")
    private long tokenExpirationMinutes;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService, UserRepository userRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email.trim().toLowerCase());
        if (optionalUser.isEmpty()) {
            return;
        }
        UserEntity user = optionalUser.get();

        String code = generateCode();
        passwordResetTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.save(
                new PasswordResetTokenEntity(user, hashCode(code), Instant.now().plus(tokenExpirationMinutes, ChronoUnit.MINUTES)));
        sendPasswordResetEmail(user.getEmail(), code);
    }

    @Transactional
    public void resetPassword(String code, String password) {
        Instant now = Instant.now();
        PasswordResetTokenEntity passwordResetToken = passwordResetTokenRepository.findByTokenHash(hashCode(code))
                .filter(resetToken -> resetToken.isUsableAt(now))
                .orElseThrow(InvalidPasswordResetTokenException::new);

        passwordResetToken.getUser().setPassword(passwordEncoder.encode(password));
        passwordResetToken.markUsed(now);
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String hashCode(String code) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private void sendPasswordResetEmail(String email, String code) {
        String body = "<p>We received a request to reset your Falcon Airlines password.</p>"
                + "<p>Your password reset code is <strong>" + code + "</strong>.</p>"
                + "<p>This code expires in " + tokenExpirationMinutes + " minutes.</p>";
        emailService.send(new EmailRequest(email, "Reset your Falcon Airlines password", body, true, List.of(), List.of()));
    }
}
