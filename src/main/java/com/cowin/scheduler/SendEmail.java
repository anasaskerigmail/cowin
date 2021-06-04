package com.cowin.scheduler;

import lombok.extern.slf4j.Slf4j;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
public class SendEmail {

   public static void sendEmail(Centers center, Session cowinSession, String emailId) {

      // Sender's email ID needs to be mentioned
      String from = "philipstestingacguser@gmail.com";

      // Assuming you are sending email from localhost
      String host = "smtp.gmail.com";

      // Get system properties
      Properties properties = System.getProperties();

      // Setup mail server
      properties.put("mail.smtp.host", host);
      properties.put("mail.smtp.port", "465");
      properties.put("mail.smtp.ssl.enable", "true");
      properties.put("mail.smtp.auth", "true");

      // Get the Session object.// and pass username and password
      javax.mail.Session session = javax.mail.Session.getInstance(properties, new javax.mail.Authenticator() {

         @Override
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("******", "*******");
         }

      });

      try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailId));

         // Set Subject: header field
         message.setSubject("Vaccination Dose Available");

         // Now set the actual message
         message.setText("Vaccination dose is available for age greater than "+ cowinSession.getMinAgeLimit() + " and date " + cowinSession.getDate()
         + " and for center " + center.getName());

         // Send message
         Transport.send(message);
         log.info("Sent message to user {} for age greater than {} and date {} and for center {}"
                 , emailId , cowinSession.getMinAgeLimit(), cowinSession.getDate(), center.getName());

      } catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }
}