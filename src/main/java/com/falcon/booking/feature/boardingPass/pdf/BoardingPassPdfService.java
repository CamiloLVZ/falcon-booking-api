package com.falcon.booking.feature.boardingPass.pdf;

import com.falcon.booking.feature.boardingPass.dto.BoardingPassView;

public interface BoardingPassPdfService {
    byte[] generate(BoardingPassView boardingPass);
}
