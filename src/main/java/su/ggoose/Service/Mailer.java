package su.ggoose.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import su.ggoose.TeleClientList;
import su.ggoose.Model.Message;
import su.ggoose.Model.MessageToSend;
import su.ggoose.Model.TeleClient;

@Service
public class Mailer {
  @Value("${error.mail}")
  private String sentTo;
  @Value("${error.time}")
  private Long minBetweenSameErrors;
  @Value("${error.sendMails}")
  private boolean isSendMails;
  private final Logger _logger = LoggerFactory.getLogger(this.getClass());
  private static JavaMailSender javaMailSender;
  private final Map<String, LocalDateTime> sentMessages = new HashMap<>();
  private final MessageToSend messageToSend;
  private final TeleClientList teleClientList;

  public Mailer(JavaMailSender jms, MessageToSend messageToSend, TeleClientList teleClientList) {
    javaMailSender = jms;
    this.messageToSend = messageToSend;
    this.teleClientList = teleClientList;
  }

  public void sendMail(String message) {
    if (sentMessages.containsKey(message)) {
      Duration duration = Duration.between(sentMessages.get(message), LocalDateTime.now());
      if (duration.toMinutes() > minBetweenSameErrors) {
        send(message);
        sentMessages.put(message, LocalDateTime.now());
      } else {
        _logger.info("Still same error -> " + message);
      }
    } else {
      sentMessages.put(message, LocalDateTime.now());
      send(message);
    }

  }

  public void orderMessage(String sub, String text) {
    SimpleMailMessage msg = new SimpleMailMessage();
    String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
    msg.setTo(sentTo);
    msg.setSubject(sub + " " + date);
    msg.setText(text);
    try {
      javaMailSender.send(msg);
    } catch (Exception e) {
      // fked if u get here :)
      _logger.error("sendMail", e);
      this.spreadTheWordAdmin("Error sending mail.. better be awake");
    }

  }

  private void send(String message) {
    if (isSendMails) {
      SimpleMailMessage msg = new SimpleMailMessage();
      String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
      msg.setTo(sentTo);
      msg.setSubject(message + " " + date);
      msg.setText(message);
      try {
        javaMailSender.send(msg);
      } catch (Exception e) {
        // fked if u get here :)
        _logger.error("sendMail", e);
        this.spreadTheWordAdmin("Error sending mail.. better be awake");
      }
    } else {
      _logger.info("Would send message >> " + message);
    }
  }

  private void spreadTheWordAdmin(String messageContent) {
    System.out.println(messageContent);
    for (TeleClient teleClient : this.teleClientList.getTeleClientList()) {
      if (teleClient.getInfoLevel() >= 100) {
        try {
          messageToSend.getMessageQueue().put(new Message(teleClient.getChatId(), messageContent));
        } catch (InterruptedException e) {
          // fked if u reach here
          _logger.error("could not put to message queue", e);
          _logger.info(teleClient.getChatId() + " " + messageContent);
        }
        break;
      }
    }
  }

  public boolean changeMailBool() {
    this.isSendMails = !this.isSendMails;
    return this.isSendMails;
  }
}
