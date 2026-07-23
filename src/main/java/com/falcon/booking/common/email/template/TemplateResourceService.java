package com.falcon.booking.common.email.template;

public interface TemplateResourceService {
    String loadCss(String path);
    String loadImageAsDataUri(String path);
}