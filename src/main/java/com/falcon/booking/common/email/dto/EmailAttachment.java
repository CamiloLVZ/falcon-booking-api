package com.falcon.booking.common.email.dto;

public record EmailAttachment(
        String fileName,
        String contentType,
        byte[] data
) { }