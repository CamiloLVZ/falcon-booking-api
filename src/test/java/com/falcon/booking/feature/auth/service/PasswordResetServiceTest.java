package com.falcon.booking.feature.auth.service;

import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.feature.auth.exception.InvalidPasswordResetTokenException;
import com.falcon.booking.persistence.entity.PasswordResetTokenEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.PasswordResetTokenRepository;
import com.falcon.booking.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "tokenExpirationMinutes", 30L);
    }

    @Test
    void shouldCreateHashedTokenAndSendEmailForExistingUser() {
        UserEntity user = new UserEntity();
        user.setEmail("client@test.com");
        given(userRepository.findByEmail("client@test.com")).willReturn(Optional.of(user));

        passwordResetService.requestPasswordReset(" Client@Test.Com ");

        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        ArgumentCaptor<EmailRequest> emailCaptor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(emailService).send(emailCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).hasSize(64);
        assertThat(tokenCaptor.getValue().getExpiresAt()).isAfter(Instant.now());
        assertThat(emailCaptor.getValue().to()).isEqualTo("client@test.com");
        assertThat(emailCaptor.getValue().body()).containsPattern("<strong>\\d{6}</strong>");
    }

    @Test
    void shouldNotRevealWhetherAnEmailExists() {
        given(userRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

        passwordResetService.requestPasswordReset("unknown@test.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).send(any());
    }

    @Test
    void shouldResetPasswordWithUsableToken() {
        UserEntity user = new UserEntity();
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(user, "hash", Instant.now().plusSeconds(60));
        given(passwordResetTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(resetToken));
        given(passwordEncoder.encode("new-password")).willReturn("encoded-password");

        passwordResetService.resetPassword("123456", "new-password");

        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(resetToken.getUsedAt()).isNotNull();
        verify(passwordEncoder).encode("new-password");
    }

    @Test
    void shouldRejectExpiredToken() {
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(new UserEntity(), "hash", Instant.now().minusSeconds(1));
        given(passwordResetTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> passwordResetService.resetPassword("123456", "new-password"))
                .isInstanceOf(InvalidPasswordResetTokenException.class);

        verify(passwordEncoder, never()).encode(anyString());
    }
}
