package com.falcon.booking.feature.boarding.pdf;

import com.falcon.booking.feature.boarding.dto.BoardingPassView;

public interface BoardingPassPdfService {
    byte[] generate(BoardingPassView boardingPass);
}
