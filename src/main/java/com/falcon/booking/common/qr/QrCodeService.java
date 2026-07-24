package com.falcon.booking.common.qr;

public interface QrCodeService {
    byte[] generate(String content);
}
