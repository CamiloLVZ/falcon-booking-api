package com.falcon.booking.common.email.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class ClasspathTemplateResourceService implements TemplateResourceService {

    private final ResourceLoader resourceLoader;

    public ClasspathTemplateResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public String loadCss(String path) {
        log.debug("Loading CSS resource from classpath: {}", path);
        try (InputStream inputStream = resourceLoader.getResource("classpath:" + path).getInputStream()) {

            String css = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.trace("Successfully loaded CSS from {}", path);
            return css;

        } catch (IOException e) {
            log.error("Failed to load CSS from classpath path: {}", path, e);
            throw new RuntimeException("Unable to load css: " + path, e);
        }
    }

    @Override
    public String loadImageAsDataUri(String path) {
        log.debug("Loading image resource as Data URI from classpath: {}", path);
        try (InputStream inputStream = resourceLoader.getResource("classpath:" + path).getInputStream()) {

            byte[] bytes = inputStream.readAllBytes();
            String dataUri = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
            log.trace("Successfully converted image to Data URI from {}", path);
            return dataUri;

        } catch (IOException e) {
            log.error("Failed to load image from classpath path: {}", path, e);
            throw new RuntimeException("Unable to load image: " + path, e);
        }

    }

}
