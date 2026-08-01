package com.falcon.booking.feature.boarding.pdf;

import com.falcon.booking.feature.boarding.dto.BoardingPassDocumentData;

import java.util.Arrays;
import java.util.Objects;

public record BoardingPassPdfResult(
        BoardingPassDocumentData document,
        byte[] pdf
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardingPassPdfResult that = (BoardingPassPdfResult) o;
        return Objects.equals(document, that.document)
                && Arrays.equals(pdf, that.pdf);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(document);
        result = 31 * result + Arrays.hashCode(pdf);
        return result;
    }

    @Override
    public String toString() {
        return "BoardingPassPdfResult[" +
                "document=" + document +
                ", pdf=" + Arrays.toString(pdf) +
                ']';
    }
}
