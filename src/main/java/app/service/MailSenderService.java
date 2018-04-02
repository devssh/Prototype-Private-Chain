package app.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

import static app.service.QRCodeService.MY_QRCODE_PNG;

public class MailSenderService {

    public static void SendMailWithQRCode(String to, String subject, String text) throws Exception {
        JavaMailSenderImpl emailSender = new JavaMailSenderImpl();
        emailSender.setHost("smtp.gmail.com");
        emailSender.setPort(587);

        emailSender.setUsername("deva3sood@gmail.com");
        emailSender.setPassword("nopass");

        Properties props = emailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        FileSystemResource file = new FileSystemResource(new File(MY_QRCODE_PNG));
        helper.addInline("QRCode", file);

        emailSender.send(message);
    }
}