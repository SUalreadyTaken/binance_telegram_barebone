package su.ggoose.Runnable;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import su.ggoose.TelegramBot;
import su.ggoose.Model.Message;
import su.ggoose.Model.MessageToSend;
import su.ggoose.Service.Mailer;

public class ExecuteMessages implements Runnable {

  private final MessageToSend messageToSend;
  private final TelegramBot telegramBot;
  private final Mailer mailer;
  private int MESSAGES_SENT = 0;
  private long LAST_MESSAGE_SENT = System.currentTimeMillis();
  private final Logger _logger = LoggerFactory.getLogger(this.getClass());
  String chatId = "";

  public ExecuteMessages(MessageToSend messageToSend, TelegramBot telegramBot, Mailer mailer) {
    this.messageToSend = messageToSend;
    this.telegramBot = telegramBot;
    this.mailer = mailer;
  }

  @Override
  public void run() {
    while (true) {
      try {
        Message message = messageToSend.getMessageQueue().take();
        chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage(message.getChatId(), message.getText());
        telegramBot.execute(sendMessage);
        sleepIfNeeded();
      } catch (InterruptedException e) {
        System.out.println("ExecuteMessage queue error");
        e.printStackTrace();
      } catch (TelegramApiException e) {
        _logger.error("Error executing message", e);
        mailer.sendMail("Error executing message");
        System.out.println("id > " + chatId);
        System.out.println();
        e.printStackTrace();
      }
      LAST_MESSAGE_SENT = System.currentTimeMillis();
    }
  }

  public void sleepIfNeeded() {
    MESSAGES_SENT++;
    if (MESSAGES_SENT >= 30) {
      try {
        TimeUnit.MILLISECONDS.sleep(1000);
        MESSAGES_SENT = 0;
      } catch (InterruptedException e) {
        System.out.println("ExecuteMessage error is sleeping");
        e.printStackTrace();
      }
    } else if (System.currentTimeMillis() - LAST_MESSAGE_SENT >= 1000) {
      MESSAGES_SENT = 1;
    }
  }

}