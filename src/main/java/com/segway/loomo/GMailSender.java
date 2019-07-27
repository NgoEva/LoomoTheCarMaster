package com.segway.loomo;

import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Vinayak Bevinakatti
 * https://stackoverflow.com/questions/2020088/sending-email-in-android-using-javamail-api-without-using-the-default-built-in-a#2033124
 *
 * The Email functionality only works for a Googlemail account that allows access from less secure apps (Google Account Setting).
 *
 * The following classes need to be imported into the Android Project:
 *  additional.jar
 *  mail.jar
 *  activation.jar
 * Source: javamail-android (https://code.google.com/archive/p/javamail-android/)
 *
 * Permission Requirements:
 *  Internet
 */


public class GMailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body,
                                      String sender, String recipients) throws Exception {
        MimeMessage message = new MimeMessage(session);
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setDataHandler(handler);

        if (recipients.indexOf(',') > 0)
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        else
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

        Transport.send(message);
    }
}