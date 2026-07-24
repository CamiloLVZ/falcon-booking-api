package com.falcon.booking.common.qr;

import com.falcon.booking.common.qr.exception.QrCodeGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZxingQrCodeServiceTest {

    private final ZxingQrCodeService qrCodeService = new ZxingQrCodeService();

    @DisplayName("Should generate a non-empty byte array for a valid URL content")
    @Test
    void shouldGenerateQrCode() {
        String content = "https://falcon.example.com/boarding-pass/validate/some-uuid";

        byte[] result = qrCodeService.generate(content);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @DisplayName("Should generate QR for plain text content")
    @Test
    void shouldGenerateQrCode_WithPlainText() {
        String content = "RESERVATION-ABC123";

        byte[] result = qrCodeService.generate(content);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @DisplayName("Should throw QrCodeGenerationException for empty content")
    @Test
    void shouldThrow_WhenContentIsEmpty() {
        assertThatThrownBy(() -> qrCodeService.generate(""))
                .isInstanceOf(QrCodeGenerationException.class);
    }
}
