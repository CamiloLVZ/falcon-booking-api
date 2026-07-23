package com.falcon.booking.common.email;

import com.falcon.booking.common.email.dto.EmailRequest;

public interface EmailService {
    void send(EmailRequest request);
}
