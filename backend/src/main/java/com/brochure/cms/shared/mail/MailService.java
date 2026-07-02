package com.brochure.cms.shared.mail;

import com.brochure.cms.config.AppProperties;
import com.brochure.cms.domain.comment.Comment;
import com.brochure.cms.domain.contact.ContactSubmission;
import com.brochure.cms.shared.exception.EmailDeliveryException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig;
    private final AppProperties appProperties;

    public MailService(JavaMailSender mailSender, Configuration freemarkerConfig, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.freemarkerConfig = freemarkerConfig;
        this.appProperties = appProperties;
    }

    public void sendMagicLink(String toEmail, String rawToken) {
        String link = buildFrontendUrl("/auth/verify?token=" + rawToken);
        Map<String, Object> model = Map.of("magicLink", link, "expiresMinutes", 15);
        sendHtmlEmail(toEmail, "Your login link", "magic-link", model);
    }

    public void sendContactNotification(String adminEmail, ContactSubmission submission) {
        Map<String, Object> model = Map.of("submission", submission);
        sendHtmlEmail(adminEmail, "New contact form submission", "contact-notification", model);
    }

    public void sendCommentReply(String authorEmail, Comment comment, Comment reply) {
        Map<String, Object> model = Map.of("comment", comment, "reply", reply);
        sendHtmlEmail(authorEmail, "Someone replied to your comment", "comment-reply", model);
    }

    public void sendWelcome(String toEmail, String displayName) {
        Map<String, Object> model = Map.of("displayName", displayName, "loginUrl", buildFrontendUrl("/auth/login"));
        sendHtmlEmail(toEmail, "Welcome to BrochureCMS", "welcome", model);
    }

    private String buildFrontendUrl(String path) {
        String base = appProperties.baseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> model) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(appProperties.mail().from());
            helper.setText(renderTemplate(templateName, model), true);
            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new EmailDeliveryException("Email delivery failed", e);
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> model) {
        try {
            Template template = freemarkerConfig.getTemplate(templateName + ".html");
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            throw new EmailDeliveryException("Failed to render email template: " + templateName, e);
        }
    }
}
