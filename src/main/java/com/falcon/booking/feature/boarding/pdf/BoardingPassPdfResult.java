package com.falcon.booking.feature.boarding.pdf;

import com.falcon.booking.feature.boarding.dto.BoardingPassDocumentData;

public record BoardingPassPdfResult(
        BoardingPassDocumentData document,
        byte[] pdf
) {
}
