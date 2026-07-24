package com.falcon.booking.common.email.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
public class ThymeleafEmailTemplateService implements EmailTemplateService {

    private static final String LOGO_CID = "falcon-logo";
    private final TemplateEngine templateEngine;
    private final String css;

    public ThymeleafEmailTemplateService(TemplateEngine templateEngine, TemplateResourceService resourceService) {
        this.templateEngine = templateEngine;
        this.css = resourceService.loadCss("templates/email/boarding-pass-email.css");
    }

    @Override
    public String process(String template, Map<String, Object> variables) {
        log.debug("Processing Thymeleaf template: {}", template);
        Context context = new Context();
        context.setVariable("logo", "cid:" + LOGO_CID);
        context.setVariable("css", css);
        variables.forEach(context::setVariable);


        String result = templateEngine.process(template, context);
        log.trace("Successfully processed Thymeleaf template: {}", template);
        return result;

    }

}