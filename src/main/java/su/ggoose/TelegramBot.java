package su.ggoose;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import su.ggoose.Model.CoinPositionV1;
import su.ggoose.Model.Message;
import su.ggoose.Model.MessageToSend;
import su.ggoose.Model.TeleClient;
import su.ggoose.Runnable.BinanceChecker;
import su.ggoose.Service.Mailer;
import su.ggoose.Service.TeleClientService;

@Component
public class TelegramBot extends TelegramLongPollingBot {
  @Value("${telegram.token}")
  private String token;

  @Value("${telegram.username}")
  private String username;

  // Just in case so no outsider gets registered
  @Value("${telegram.pass}")
  private String pass;

  private final Logger _logger = LoggerFactory.getLogger(this.getClass());

  private final Map<String, CoinPositionV1> coinMap;
  private final MessageToSend messageToSend;
  private final TeleClientList teleClientList;
  private final TeleClientService teleClientService;
  private final Mailer mailer;
  // private final BinanceChecker binanceChecker;

  public TelegramBot(BinanceChecker binanceChecker, MessageToSend messageToSend, TeleClientList teleClientList,
      TeleClientService teleClientService, Mailer mailer) {
    this.coinMap = binanceChecker.getCoinMap();
    this.messageToSend = messageToSend;
    this.teleClientList = teleClientList;
    this.teleClientService = teleClientService;
    this.mailer = mailer;
    // this.binanceChecker = binanceChecker;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {

      String message = update.getMessage().getText();
      String chatId = Long.toString(update.getMessage().getChatId());
      StringBuilder respondMessage = new StringBuilder();

      _logger.info("Got this msg >> " + update.getMessage().getText() + " | from > " + chatId);

      String[] command = message.trim().replaceAll("\\s+", " ").split(" ");
      switch (command[0]) {
        case "/unregister":
          this.unRegister(respondMessage, chatId);
          break;
        case "/register":
          this.register(command, respondMessage, chatId);
          break;
        case "/status":
          this.getStatus(respondMessage, chatId);
          break;
        case "/mail":
          this.changeMail(respondMessage, chatId);
          break;
        default:
          break;
      }

      if (!respondMessage.toString().isEmpty()) {
        try {
          messageToSend.getMessageQueue().put(new Message(chatId, respondMessage.toString()));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        System.out.println("Respond message is empty");
      }
    }
  }

  private void changeMail(StringBuilder respondMessage, String chatId) {
    boolean change = this.mailer.changeMailBool();
    respondMessage.append(change);
  }

  private void getStatus(StringBuilder respondMessage, String chatId) {
    for(Entry<String, CoinPositionV1> coinEntry : coinMap.entrySet()) {
      respondMessage.append(coinEntry.getValue().telegramMessage());
    }
  }

  private void register(String[] command, StringBuilder respondMessage, String chatId) {
    boolean alreadyRegistered = false;

    if (command.length >= 2 && command[1].equals(pass)) {
      _logger.info("Got register message  ... " + chatId);
      try {
        for(int i = 0; i < command.length; i ++) {
          _logger.info(command[i]);
        }
      } catch (Exception e) {
        _logger.info("Some1 messed up");
        _logger.info(e.getLocalizedMessage());
      }
      for (TeleClient c : this.teleClientList.getTeleClientList()) {
        if (chatId.equals(c.getChatId())) {
          alreadyRegistered = true;
          break;
        }
      }
      if (!alreadyRegistered) {
        TeleClient t = new TeleClient(chatId, 10, LocalDateTime.now());
        this.teleClientList.getTeleClientList().add(t);
        respondMessage.append("Welcome!");
        try {
          this.teleClientService.insert(t);
        } catch (Exception e) {
          e.printStackTrace();
          mailer.sendMail("Could not register");
        }
      }
    }
  }

  private void unRegister(StringBuilder respondMessage, String chatId) {
  }

  @Override
  public String getBotUsername() {
    return username;
  }

  @Override
  public String getBotToken() {
    return token;
  }
}