package com.falcon.booking.common.qr.exception;

public class QrCodeGenerationException extends RuntimeException {
    public QrCodeGenerationException(String message) {
        super("Unable to create QR code: "+message);
    }
}
