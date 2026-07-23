package com.falcon.booking.feature.boardingPass.pdf;

import com.falcon.booking.feature.boardingPass.dto.BoardingPassView;
import com.falcon.booking.feature.boardingPass.pdf.exception.PdfGenerationException;
import com.falcon.booking.feature.boardingPass.resources.ResourceService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
public class HtmlBoardingPassService implements BoardingPassPdfService {

    private final TemplateEngine templateEngine;
    private final ResourceService resourceService;

    public HtmlBoardingPassService(TemplateEngine templateEngine, ResourceService resourceService) {
        this.templateEngine = templateEngine;
        this.resourceService = resourceService;
    }

    @Override
    public byte[] generate(BoardingPassView boardingPass) {
        log.info("Generating PDF Boarding Pass for reservation: {}", boardingPass.reservationNumber());
        Context context = new Context();
        context.setVariable("boardingPass", boardingPass);
        context.setVariable("css", resourceService.loadAsString("static/css/boarding-pass.css"));

        String html = templateEngine.process("boarding-pass", context);
        log.debug("Successfully generated HTML for boarding pass.");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();
            log.info("Successfully generated PDF Boarding Pass for reservation: {} ({} bytes)", boardingPass.reservationNumber(), pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Failed to generate PDF boarding pass for reservation: {} due to Exception: {}", boardingPass.reservationNumber(), e.getMessage(), e);
            throw new PdfGenerationException("Unable to generate boarding pass.", e);
        }
    }
}