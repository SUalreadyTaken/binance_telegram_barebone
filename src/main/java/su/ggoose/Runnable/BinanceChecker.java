package su.ggoose.Runnable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import su.ggoose.TeleClientList;
import su.ggoose.Model.CoinPositionV1;
import su.ggoose.Model.IdPercentage;
import su.ggoose.Model.Message;
import su.ggoose.Model.MessageToSend;
import su.ggoose.Model.Order;
import su.ggoose.Model.StrategyV1;
import su.ggoose.Model.TeleClient;
import su.ggoose.Model.Wallet;
import su.ggoose.Model.TeleClient.StrategyClient;
import su.ggoose.Service.MarketService;
import su.ggoose.Service.CoinPositionV1Service;
import su.ggoose.Service.Mailer;
import su.ggoose.Service.StrategyService;
import su.ggoose.Service.StrategyService.CoinChanged;
import su.ggoose.Service.StrategyV1Service;
import su.ggoose.Service.TeleClientService;
import su.ggoose.Service.WalletService;

@Component
public class BinanceChecker {

  private final Logger _logger = LoggerFactory.getLogger(this.getClass());

  private final TeleClientList teleClientList;
  private final StrategyV1Service strategyV1Service;
  private final CoinPositionV1Service coinPositionV1Service;
  private final MarketService marketService;
  private final WalletService walletService;
  private final MessageToSend messageToSend;
  private final TeleClientService teleClientService;
  private final Mailer mailer;
  private StrategyService strategyService;
  private Map<String, StrategyV1> strategyV1Map;
  private Map<String, CoinPositionV1> coinPositionV1Map = new HashMap<>();
  private Map<String, CoinPositionV1> tmpMap = new HashMap<>();
  private Map<String, List<IdPercentage>> idPercentagesMap = new HashMap<>();
  private boolean apiOk = false;

  public BinanceChecker(TeleClientList teleClientList, StrategyV1Service strategyV1Service,
      CoinPositionV1Service coinPositionV1Service, MarketService marketService, MessageToSend messageToSend,
      WalletService walletService, Mailer mailer, TeleClientService teleClientService) {
    this.teleClientList = teleClientList;
    this.strategyV1Service = strategyV1Service;
    this.coinPositionV1Service = coinPositionV1Service;
    this.marketService = marketService;
    this.messageToSend = messageToSend;
    this.walletService = walletService;
    this.mailer = mailer;
    this.teleClientService = teleClientService;
  }

  @PostConstruct
  private void initLists() {
    this.strategyV1Map = strategyV1Service.getAll();
    this.coinPositionV1Map = coinPositionV1Service.getLatestV1s(strategyV1Map);
    this.strategyService = new StrategyService(marketService);
    for (Entry<String, CoinPositionV1> coinEntry : this.coinPositionV1Map.entrySet()) {
      if (!this.strategyService.addToMap(coinEntry.getKey())) {
        _logger.error("Duplicate key exiting");
        System.exit(100);
      }
      _logger.info(coinEntry.getValue().toString());
    }

    List<TeleClient> teleList = this.teleClientService.getTeleClients();
    for (TeleClient teleClient : teleList) {
      for (StrategyClient strategyClient : teleClient.getStrategies()) {
        if (!this.idPercentagesMap.keySet().contains(strategyClient.getName())) {
          _logger.info("Doesn't contain " + strategyClient.getName());
          this.idPercentagesMap.put(strategyClient.getName(),
              new ArrayList<>(Arrays.asList(new IdPercentage(teleClient.getChatId(), strategyClient.getPercentage()))));
        } else {
          _logger.info("Already contains " + strategyClient.getName());
          this.idPercentagesMap.get(strategyClient.getName())
              .add(new IdPercentage(teleClient.getChatId(), strategyClient.getPercentage()));
        }
      }
    }
  }

  public void setApiOk(boolean isApiOk) {
    this.apiOk = isApiOk;
  }

  // TODO fix inital delay.. should be when application ready.. and then start it
  // or just use a boolean ?
  @Scheduled(fixedDelay = 1000, initialDelay = 5000)
  public void runV1() {
    if (this.apiOk) {
      for (Entry<String, CoinPositionV1> coinEntry : this.coinPositionV1Map.entrySet()) {
        CoinPositionV1 coin = coinEntry.getValue();
        StrategyV1 strat = this.strategyV1Map.get(coin.getStratName());
        if (strat != null) {
          CoinChanged coinChanged = this.strategyService.getCoinChanged(coin, strat);
          if (coinChanged.isChanged()) {
            _logger.info("coin changed should do something ");
          }
          // TODO rework delete the error
          if (coinChanged.getErrorMessage() != null) {
            mailer.sendMail(coinChanged.getErrorMessage());
          }

          if (coin.getPosition().equals("LONG")) {
            if (coinChanged.isChanged()) {
              closePosition(coin, false);
            } else if (coinChanged.isStopHit()) {
              // got stopped out
              closePosition(coin, true);
            }
          } else if (coin.getPosition().equals("CLOSED")) {
            if (coin.isStopHit()) {
              // stop was hit.. wallet etc has already been dealt with just need to wait it to
              // close organically
              if (coinChanged.isChanged()) {
                // its been dealt with just need to update coinPosition
                coin.setStopHit(false);
                CoinPositionV1 sameCoin = this.coinPositionV1Service.save(coin);
                tmpMap.put(coinEntry.getKey(), sameCoin);
                spreadTheWordLvl100(sameCoin.getStratName(), "✅ Closed by algo " + sameCoin.telegramMessage());
              }
            } else {
              if (coinChanged.isChanged()) {
                openNewPosition(coinEntry, coin, strat);
              }
            }
          } else {
            _logger.error("Coin position wrong " + coin.telegramMessage());
            mailer.sendMail("Coin position wrong ");
          }
        } else {
          _logger.error("Cant find strat for " + coinEntry.getValue().getSymbol());
          mailer.sendMail("Cant find strat");
        }
      }
    } else {
      this.apiOk = this.strategyService.getApiOk();
    }

    if (!tmpMap.isEmpty()) {
      for (Entry<String, CoinPositionV1> coin : tmpMap.entrySet()) {
        this.coinPositionV1Map.replace(coin.getKey(), coin.getValue());
      }
      tmpMap.clear();
    }

  }

  private List<IdPercentage> getStratPercentages(String stratName) {
    for (Entry<String, List<IdPercentage>> perEntry : this.idPercentagesMap.entrySet()) {
      if (perEntry.getKey().equals(stratName)) {
        return perEntry.getValue();
      }
    }
    return null;
  }

  private void openNewPosition(Entry<String, CoinPositionV1> coinEntry, CoinPositionV1 coin, StrategyV1 strat) {
    Wallet stratWallet = walletService.getWallet(strat.getStratName());
    if (stratWallet != null) {
      String dollarsToSpend = stratWallet.getCash();
      Order buyOrder = marketService.createBuyOrderMarket(coin.getSymbol(), dollarsToSpend);
      if (buyOrder != null) {
        System.out.println("buyOrder > " + buyOrder.toString());
        String fillPrice = buyOrder.getPrice();
        // FIXME
        /*
         * ## REMOVED TRADE SECRET ##
         */
        String stopPrice = Float.toString((Float.parseFloat(fillPrice) * ((100f - 1f) / 100)));

        List<IdPercentage> percentagesList = getStratPercentages(strat.getStratName());
        if ((percentagesList != null && percentagesList.isEmpty()) || percentagesList == null) {
          _logger.error("Percentage list is fked up");
          this.mailer.sendMail("Percentage list is fked up");
        }

        CoinPositionV1 newPos = new CoinPositionV1("LONG", buyOrder.getOrderId(), buyOrder.getAmount(),
            Float.parseFloat(buyOrder.getPrice()),
            coin.getSymbol(), false, Float.parseFloat(stopPrice), strat.getStratName(),
            LocalDateTime.now(), percentagesList);
        String teleMessage = newPos.telegramMessage();
        // NOTICE change back to 100 when the time is right
        spreadTheWordLvl10(strat.getStratName(), "📈 Position changed\n" + teleMessage);
        try {
          CoinPositionV1 coinWithId = coinPositionV1Service.insert(newPos);
          tmpMap.put(coinEntry.getKey(), coinWithId);
        } catch (Exception e) {
          _logger.error("Opened new pos without id ", e);
          tmpMap.put(coinEntry.getKey(), newPos);
          mailer.sendMail("Opened new pos without id " + coin.getStratName());
        }
      } else {
        _logger.error("Buy order got fked");
        mailer.sendMail("Buy order got fked " + coin.getStratName());
      }
    } else {
      _logger.error("Didn't find strat wallet");
      mailer.sendMail("Didn't find strat wallet" + coin.getStratName());
    }
  }

  private void closePosition(CoinPositionV1 coin, boolean stopHit) {
    Order order = this.marketService.createSellOrderMarket(coin.getSymbol(), coin.getAmount());
    if (order != null) {
      // all good
      System.out.println("closePosition order " + order.toString());
      List<IdPercentage> percentagesList = getStratPercentages(coin.getStratName());
      if ((percentagesList != null && percentagesList.isEmpty()) || percentagesList == null) {
        _logger.error("Percentage list is fked up");
        this.mailer.sendMail("Percentage list is fked up");
      }

      CoinPositionV1 coinPositionV1 = new CoinPositionV1("CLOSED", order.getOrderId(), order.getAmount(),
          Float.parseFloat(order.getPrice()), coin.getSymbol(), stopHit, 0, coin.getStratName(),
          LocalDateTime.now(), percentagesList);
      String message = stopHit ? "🤝⛔ Position changed\n" : "🤝 Position changed\n";
      CoinPositionV1 coinWithId = null;
      String oldPos = coin.telegramMessage();
      spreadTheWordLvl10(coin.getStratName(),
          message + oldPos + "\n" + coinPositionV1.telegramMessage());
      try {
        coinWithId = this.coinPositionV1Service.insert(coinPositionV1);
        tmpMap.put(coin.getStratName(), coinWithId);
      } catch (Exception e) {
        _logger.info(coinPositionV1.toString());
        tmpMap.put(coin.getStratName(), coinPositionV1);
        _logger.error("Closed position with no id", e);
        mailer.sendMail("Closed position with no id " + coin.getStratName());
      }
      updateWallet(coin, order);
    } else {
      _logger.error("closePosition wasn't executed");
      mailer.sendMail("closePosition wasn't executed " + coin.getStratName());
    }

  }

  public void updateWallet(CoinPositionV1 coin, Order order) {
    float openCost = Float.parseFloat(coin.getAmount()) * coin.getPrice();
    float profit = (Float.parseFloat(order.getAmount()) * Float.parseFloat(order.getPrice())) - openCost;
    Wallet wallet = this.walletService.getWallet(coin.getStratName());
    if (wallet != null) {
      String newCash = Float.toString(Float.parseFloat(wallet.getCash()) + profit);
      wallet.setCash(newCash);
      try {
        walletService.save(wallet);
      } catch (Exception e) {
        _logger.error("Error updating newCash for wallet", e);
        mailer.sendMail("Error updating newCash for waller " + coin.getStratName());
      }
    } else {
      _logger.error("DIDN't find wallet " + coin.getStratName());
      mailer.sendMail("Didn't find wallet" + coin.getStratName());
    }
  }

  private void spreadTheWordLvl10(String stratName, String messageContent) {
    for (TeleClient teleClient : this.teleClientList.getTeleClientList()) {
      if (teleClient.getInfoLevel() >= 10) {
        boolean needToSend = teleClient.getStrategies().stream().filter(strat -> strat.getName().equals(stratName))
            .findFirst().isPresent();
        if (needToSend) {
          try {
            messageToSend.getMessageQueue().put(new Message(teleClient.getChatId(), messageContent));
          } catch (InterruptedException e) {
            _logger.error("could not put to message queue", e);
            _logger.info(teleClient.getChatId() + " " + messageContent);
            mailer.sendMail("Message didn't go to queue");
          }
        }
      }
    }
  }

  private void spreadTheWordLvl100(String stratName, String messageContent) {
    for (TeleClient teleClient : this.teleClientList.getTeleClientList()) {
      if (teleClient.getInfoLevel() >= 100) {
        boolean needToSend = teleClient.getStrategies().stream().filter(strat -> strat.getName().equals(stratName))
            .findFirst().isPresent();
        if (needToSend) {
          try {
            messageToSend.getMessageQueue().put(new Message(teleClient.getChatId(), messageContent));
          } catch (InterruptedException e) {
            _logger.error("could not put to message queue", e);
            _logger.info(teleClient.getChatId() + " " + messageContent);
            mailer.sendMail("Message didn't go to queue that was meant to u");
          }

        }
      }
    }
  }

  public Map<String, CoinPositionV1> getCoinMap() {
    return this.coinPositionV1Map;
  }

  public void sendWallet() {
    spreadTheWordLvl100("BTCUSDT_v1", walletService.getWallet("BTCUSDT_v1").toString());
  }

  public String getXxxStatus(String strat) {
    return this.strategyService.getXxxStatus(strat);
  }

}
