package su.ggoose.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;
import su.ggoose.Model.IndiOne;
import su.ggoose.Model.Candlestick;
import su.ggoose.Model.CoinPositionV1;
import su.ggoose.Model.IndiTwo;
import su.ggoose.Model.StrategyV1;
import su.ggoose.Utils.IndicatorOneUtil;
import su.ggoose.Utils.IndicatorTwoUtil;

public class StrategyService {
  private final Logger _logger = LoggerFactory.getLogger(this.getClass());
  private final IndicatorOneUtil indicatorOneUtil = new IndicatorOneUtil();
  private final IndicatorTwoUtil indicatorTwoUtil = new IndicatorTwoUtil();
  private MarketService binanceDataService;
  // TODO put to db and get it there
  String intervalV1 = "15m";
  private Map<String, List<Candlestick>> candleMap = new HashMap<>();

  public StrategyService(MarketService binanceDataService) {
    this.binanceDataService = binanceDataService;
  }

  public boolean addToMap(String strat) {
    if (!candleMap.containsKey(strat)) {
      candleMap.put(strat, new ArrayList<>());
      return true;
    }
    return false;
  }

  public String getXxxStatus(String startName) {
    String result = "xxx";
    // FIXME
    /*
     * ## REMOVED TRADE SECRET ##
     */

    return result;
  }

  public boolean getApiOk() {
    return binanceDataService.isApiOk;
  }

  public CoinChanged getCoinChanged(CoinPositionV1 coinPosition, StrategyV1 strat) {
    if (candleMap.containsKey(strat.getStratName())) {
      // FIXME
      /*
       * ## REMOVED TRADE SECRET ##
       */

      int limit = 900;
      List<Candlestick> mappedCandlelist = candleMap.get(strat.getStratName());
      try {
        if (mappedCandlelist.size() < 673 && !mappedCandlelist.isEmpty()) {
          // get only 2 candles
          List<Candlestick> candleList = binanceDataService.getCandleListV1(2, intervalV1, strat.getSymbolName());
          if (!candleList.get(0).getDate().isEqual(mappedCandlelist.get(mappedCandlelist.size() - 1).getDate())) {
            mappedCandlelist.add(candleList.get(0));
          }
          return checkV1Change(strat, mappedCandlelist, coinPosition, candleList.get(1));
        } else {
          candleMap.get(strat.getStratName()).clear();
          List<Candlestick> candleList = binanceDataService.getCandleListV1(limit, intervalV1, strat.getSymbolName());
          Candlestick lastCandle = candleList.get(candleList.size() - 1);
          candleList.remove(candleList.size() - 1);
          candleMap.put(strat.getStratName(), candleList);
          return checkV1Change(strat, candleList, coinPosition, lastCandle);
        }
      } catch (Exception e) {
        _logger.error("getCandleListV1 fked up");
        System.out.println(e);
      }
    } else {
      _logger.error("stratMap doesn't contain " + coinPosition.getStratName());
    }

    return null;
  }

  private CoinChanged checkV1Change(StrategyV1 strat, List<Candlestick> candleList, CoinPositionV1 coinPosition,
      Candlestick lastCandle) {
    // TODO should just save them and reuse.. small but still
    // FIXME
    /*
     * ## REMOVED TRADE SECRET ##
     */

    if (candleList.size() > (100 + 1)) {
      IndiOne indiOne = this.indicatorOneUtil.getIndicatorOneData(10, candleList);
      // FIXME
      /*
       * ## REMOVED TRADE SECRET ##
       */

      IndiTwo indiTwo = indicatorTwoUtil.getIndicatorTwoData(candleList);
      // NOTICE -1 is last candle that isnt closed yet
      int index = candleList.size() - 1;
      if (coinPosition.getPosition().equals("CLOSED")) {
        if (coinPosition.isStopHit()) {
          // stop hit wait for it to close organically
          boolean changed = this.isCloseLong();
          return new CoinChanged(changed, false);
        } else {
          // stop wasn't hit closed organically.. look for long
          boolean changed = this.isOpenLong();
          return new CoinChanged(changed, false);
        }
      } else {
        boolean stopHit = false;
        if (lastCandle.getDate().isAfter(coinPosition.getDate())) {
          if (lastCandle.getLow() != 0) {
            stopHit = lastCandle.getLow() <= coinPosition.getStopPrice() ? true : false;
            if (stopHit) {
              _logger.info("Stop > " + coinPosition.getStopPrice() + " current low > " + lastCandle.getLow());
            }
          } else {
            _logger.error("Last candle low is 0");
            CoinChanged c = new CoinChanged(false, stopHit);
            c.setError(true);
            c.setErrorMessage("Last candle low is 0");
            return c;
          }
        }

        // close long organically ?
        boolean changed = this.isCloseLong();
        return new CoinChanged(changed, stopHit);
      }
    }
    return null;
  }

  private boolean isCloseLong() {
    // FIXME
    /*
     * ## REMOVED TRADE SECRET ##
     */
    return (ThreadLocalRandom.current().nextInt(1, 10) % 2) == 0 ? true : false;
  }

  private boolean isOpenLong() {
    // FIXME
    /*
     * ## REMOVED TRADE SECRET ##
     */
    return (ThreadLocalRandom.current().nextInt(1, 10) % 1) == 0 ? true : false;
  }

  @Getter
  public class CoinChanged {
    private boolean changed;
    private boolean stopHit;
    @Setter
    private boolean error;
    @Setter
    private String errorMessage;

    public CoinChanged(boolean changed, boolean stopHit) {
      this.changed = changed;
      this.stopHit = stopHit;
    }
  }
}
