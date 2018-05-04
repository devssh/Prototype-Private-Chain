package app.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;
import java.util.Scanner;

public class MailSenderService {

    public static void sendMail(String to, String subject, String text, InputStreamSource inputStreamSource) throws Exception {
        JavaMailSenderImpl emailSender = new JavaMailSenderImpl();
        emailSender.setHost("smtp.gmail.com");
        emailSender.setPort(587);
        Scanner scanner = new Scanner(new File("password.dat"));
        //Enter email password on separate lines in prototype-private-chain/password.dat, it is gitignored.
        emailSender.setUsername(scanner.nextLine());
        emailSender.setPassword(scanner.nextLine());

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

        helper.addAttachment("coupon.pkpass", inputStreamSource);
        emailSender.send(message);
    }
}