package com.falcon.booking.common.qr;

import com.falcon.booking.common.qr.exception.QrCodeGenerationException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ZxingQrCodeService implements QrCodeService {

    private static final int DEFAULT_QR_SIZE = 300;
    private static final String IMAGE_FORMAT = "png";

    @Override
    public byte[] generate(String content) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BitMatrix matrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
            MatrixToImageWriter.writeToStream(matrix, IMAGE_FORMAT, outputStream);
        } catch (WriterException | IOException | IllegalArgumentException e) {
            throw new QrCodeGenerationException(e.getMessage());
        }

        return outputStream.toByteArray();
    }
}
