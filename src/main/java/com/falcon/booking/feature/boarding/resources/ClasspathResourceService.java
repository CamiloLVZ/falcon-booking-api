package com.falcon.booking.feature.boarding.resources;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
public class ClasspathResourceService implements ResourceService {

    @Override
    public String loadAsString(String path) {
        return new String(loadAsBytes(path), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] loadAsBytes(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return resource.getInputStream().readAllBytes();

        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load resource: " + path, e);
        }
    }

}
