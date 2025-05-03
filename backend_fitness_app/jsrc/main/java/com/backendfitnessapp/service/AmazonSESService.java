package com.backendfitnessapp.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.lambda.runtime.Context;
import software.amazon.awssdk.regions.Region;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

public class AmazonSESService {
    private static final String SENDER = "Plus.061999@gmail.com";
    private static final String RECIPIENT = "Mukhammadmunir_Akhmadi@student.itpu.uz";
    private static final String SUBJECT = "Weekly Gym & Coach Performance Reports";

    private static final String BODY_TEXT = "Hello,\r\n"
            + "Please find attached the weekly reports for gym performance and coach statistics.";

    private static final String BODY_HTML = "<html>"
            + "<head></head>"
            + "<body>"
            + "<h1>Hello!</h1>"
            + "<p>Please find attached the weekly reports for gym performance and coach statistics.</p>"
            + "</body>"
            + "</html>";

    public void sendMessage(String gymReportFilePath, String coachReportFilePath, Context context) throws MessagingException, IOException {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        message.setSubject(SUBJECT, "UTF-8");
        message.setFrom(new InternetAddress(SENDER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT));

        MimeMultipart msgBody = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(BODY_TEXT, "text/plain; charset=UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(BODY_HTML, "text/html; charset=UTF-8");

        msgBody.addBodyPart(textPart);
        msgBody.addBodyPart(htmlPart);

        MimeBodyPart wrap = new MimeBodyPart();
        wrap.setContent(msgBody);

        MimeMultipart msg = new MimeMultipart("mixed");
        message.setContent(msg);
        msg.addBodyPart(wrap);

        addAttachment(msg, gymReportFilePath);
        addAttachment(msg, coachReportFilePath);

        try {
            System.out.println("Sending email with two CSV reports...");

            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(System.getenv("REGION"))
                    .build();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);

            client.sendRawEmail(rawEmailRequest);
            System.out.println("Email sent successfully!");

        } catch (Exception ex) {
            context.getLogger().log("Email sending failed: " + ex.getMessage());
            System.err.println("Email sending failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addAttachment(MimeMultipart msg, String filePath) throws MessagingException {
        if (filePath == null || filePath.isEmpty()) {
            System.err.println("File path is empty, skipping attachment.");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return;
        }

        MimeBodyPart attachment = new MimeBodyPart();
        DataSource source = new FileDataSource(filePath);
        attachment.setDataHandler(new DataHandler(source));
        attachment.setFileName(file.getName());

        msg.addBodyPart(attachment);
    }
}
