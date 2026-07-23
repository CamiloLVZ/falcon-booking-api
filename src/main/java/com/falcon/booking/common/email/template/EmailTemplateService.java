package com.falcon.booking.common.email.template;

import java.util.Map;

public interface EmailTemplateService {
    String process(String template, Map<String, Object> variables);
}
