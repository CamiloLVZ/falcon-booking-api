package com.falcon.booking.common.email.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ThymeleafEmailTemplateService implements EmailTemplateService {

    private static final String LOGO_CID = "falcon-logo";
    private static final String FALLBACK_CSS_PATH = "templates/email/boarding-pass-email.css";

    private final TemplateEngine templateEngine;
    private final TemplateResourceService resourceService;
    private final Map<String, String> cssCache = new ConcurrentHashMap<>();

    public ThymeleafEmailTemplateService(TemplateEngine templateEngine, TemplateResourceService resourceService) {
        this.templateEngine = templateEngine;
        this.resourceService = resourceService;
    }

    @Override
    public String process(String template, Map<String, Object> variables) {
        log.debug("Processing Thymeleaf template: {}", template);
        String css = cssCache.computeIfAbsent(template, this::loadCssForTemplate);
        Context context = new Context();
        context.setVariable("logo", "cid:" + LOGO_CID);
        context.setVariable("css", css);
        variables.forEach(context::setVariable);

        String result = templateEngine.process(template, context);
        log.trace("Successfully processed Thymeleaf template: {}", template);
        return result;
    }

    private String loadCssForTemplate(String template) {
        String cssPath = "templates/" + template + ".css";
        try {
            String css = resourceService.loadCss(cssPath);
            log.debug("Loaded CSS for template '{}' from '{}'" , template, cssPath);
            return css;
        } catch (Exception e) {
            log.warn("No CSS found for template '{}' at '{}', falling back to default", template, cssPath);
            return resourceService.loadCss(FALLBACK_CSS_PATH);
        }
    }

}