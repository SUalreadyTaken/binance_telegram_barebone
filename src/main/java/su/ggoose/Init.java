package su.ggoose;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import su.ggoose.Model.MessageToSend;
import su.ggoose.Runnable.ExecuteMessages;
import su.ggoose.Service.Mailer;

@Component
public class Init {
  private BotSession botSession;
  private TelegramBotsApi botsApi;  
  private final TelegramBot telegramBot;
  private final MessageToSend messageToSend;
  private final Mailer mailer;

  public Init(TelegramBot telegramBot, MessageToSend messageToSend, Mailer mailer) {
    this.telegramBot = telegramBot;
    this.messageToSend = messageToSend;
    this.mailer = mailer;
    try {
      this.botsApi = new TelegramBotsApi(DefaultBotSession.class);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @PostConstruct
  private void start() {
    try {
      this.botSession = botsApi.registerBot(telegramBot);
      System.out.println("Register bot success !\nusername >> " + telegramBot.getBotUsername());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    new Thread(new ExecuteMessages(messageToSend, telegramBot, mailer)).start();
  }
}
