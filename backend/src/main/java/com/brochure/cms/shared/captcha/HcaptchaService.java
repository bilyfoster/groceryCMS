package com.brochure.cms.shared.captcha;

import com.brochure.cms.shared.exception.ValidationException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class HcaptchaService {

    private static final Logger log = LoggerFactory.getLogger(HcaptchaService.class);

    private final String secret;
    private final RestTemplate restTemplate;

    public HcaptchaService(
            @Value("${app.hcaptcha.secret:}") String secret, RestTemplateBuilder builder) {
        this.secret = secret;
        this.restTemplate = builder.build();
    }

    public void verifyIfConfigured(String token) {
        if (!StringUtils.hasText(secret)) {
            return;
        }
        if (!StringUtils.hasText(token)) {
            throw new ValidationException("CAPTCHA verification required");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    "https://hcaptcha.com/siteverify",
                    Map.of("secret", secret, "response", token),
                    Map.class);
            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                throw new ValidationException("CAPTCHA verification failed");
            }
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("hCaptcha verification error: {}", e.getMessage());
            throw new ValidationException("CAPTCHA verification failed");
        }
    }
}
