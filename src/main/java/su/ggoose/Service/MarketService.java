package su.ggoose.Service;

import java.io.Console;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.binance.connector.client.impl.SpotClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import su.ggoose.Crypt.Encrypt;
import su.ggoose.Model.Candlestick;
import su.ggoose.Model.Fills;
import su.ggoose.Model.Order;

@Service
public class MarketService {
  private static SpotClientImpl _client;
  private final Logger _logger = LoggerFactory.getLogger(this.getClass());

  public boolean isApiOk = false;

  @Value("${binance.key}")
  String binanceKeyEnc;
  @Value("${binance.secret}")
  String binanceSecretEnc;

  private final Mailer mailer;

  public MarketService(Mailer mailer) {
    this.mailer = mailer;
  }
  
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    Console console = System.console();
    if (console == null) {
      System.out.println("Couldn't get Console instance");
      System.exit(0);
    }
    char[] passwordArray = console.readPassword("Enter password: ");
    String pass = new String(passwordArray);
    try {
      String key = Encrypt.decrypt(binanceKeyEnc, pass);
      String secret = Encrypt.decrypt(binanceSecretEnc, pass);
      if (testConnection(key, secret)) {
        isApiOk = true;
        _logger.info("Pass ok, api works");
      }
      try {
        test();
      } catch (Exception e) {
        _logger.error("Test error", e);
      }
    } catch (Exception e) {
      _logger.error("Api or pass wrong", e);
    }

  }

  /**
   * Test connection. Set client if works
   * 
   * @param key
   * @param secret
   * @return
   */
  public static boolean testConnection(String key, String secret) {
    // SpotClientImpl clientTmp = new SpotClientImpl(key, secret,
    //     "https://testnet.binance.vision");
    SpotClientImpl clientTmp = new SpotClientImpl(key, secret);

    LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
    parameters.put("timestamp", System.currentTimeMillis());

    try {
      clientTmp.createTrade().account(parameters);
      _client = clientTmp;
    } catch (Exception e) {
      System.out.println(e.getStackTrace().toString());
      return false;
    }

    return true;
  }

  /**
   * Last candle not included
   * 
   * @param limit
   * @param interval
   * @param symbol
   * @return List<Candlestick>, null if api client hasn't been initialized
   */
  public List<Candlestick> getCandleListV1(int limit, String interval, String symbol) {
    List<Candlestick> result = null;
    if (this.isApiOk) {
      LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
      parameters.put("symbol", symbol);
      parameters.put("limit", limit);
      parameters.put("interval", interval);

      try {
        String klines = _client.createMarket().klines(parameters);
        result = convertToCandleList(klines);
      } catch (Exception e) {
        System.out.println(e.getStackTrace());
      }
    }
    return result;
  }

  public Order createSellOrderMarket(String symbol, String amountToSell) {
    if (this.isApiOk) {
      LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
      parameters.put("symbol", symbol);
      parameters.put("quantity", amountToSell);
      parameters.put("side", "SELL");
      parameters.put("type", "MARKET");
      parameters.put("timestamp", System.currentTimeMillis());

      String order = "";
      try {
        order = _client.createTrade().newOrder(parameters);
        _logger.info("Sell order");
        _logger.info(order);

      } catch (Exception e) {
        _logger.error("sell order didn't execute", e);
        return null;
      }

      if (!order.isBlank()) {
        return getOrderFilledSell(order);
      }
    }
    return null;
  }

  public Order createBuyOrderMarket(String symbol, String dollarsToBuyWith) {
    if (this.isApiOk) {
      LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
      parameters.put("symbol", symbol);
      parameters.put("quoteOrderQty", dollarsToBuyWith);
      parameters.put("side", "BUY");
      parameters.put("type", "MARKET");
      parameters.put("timestamp", System.currentTimeMillis());

      String order = "";
      try {
        order = _client.createTrade().newOrder(parameters);
        _logger.info("Buy order");
        _logger.info(order);

      } catch (Exception e) {
        _logger.error("buy order didn't execute", e);
        return null;
      }

      if (!order.isBlank()) {
        return getOrderFilledBuy(order);
      }
    }
    return null;
  }

  private Order getOrderFilledSell(String order) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> orderMap = mapper.readValue(order, Map.class);
      if (isOrderProperties(orderMap)) {
        String status = orderMap.get("status").toString();
        // TODO change back .. EXPIRED means no liquidity.. testnet is fked
        // make a mehtod just in case if expired remember the order id and get the
        // amount that wasn't executed and make another order
        if (!status.equals("FILLED")) {
          _logger.error("market sell wasn't filled was " + order.toString());
          mailer.sendMail("market sell wasn't filled");
        }
        // if (status.equals("FILLED")) {
        String amount = orderMap.get("executedQty").toString();
        String orderId = orderMap.get("orderId").toString();
        String symbol = orderMap.get("symbol").toString();
        String side = orderMap.get("side").toString();
        String price = Float.toString(fillsAvgPrice(order));
        return new Order(symbol, orderId, side, price, amount);
        // } else {
        // _logger.error("market sell should have filled .. why didnt it ??? " +
        // orderMap.toString());
        // }
      } else {
        _logger.error("Something wrong with sell orderMap");
        _logger.info(orderMap.toString());
      }

    } catch (Exception e) {
      _logger.error("createSellOrderMarket", e);
    }
    return null;
  }

  private Order getOrderFilledBuy(String order) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> orderMap = mapper.readValue(order, Map.class);
      if (isOrderProperties(orderMap) && orderMap.containsKey("fills")) {
        String status = orderMap.get("status").toString();
        // TODO change back .. EXPIRED means no liquidity.. testnet is fked
        // make a mehtod just in case if expired remember the order id and get the
        // amount that wasnt executed and ma ke another order
        if (!status.equals("FILLED")) {
          _logger.error("market buy wasn't filled was " + order.toString());
          mailer.sendMail("market buy wasn't filled");
        }
        // if (status.equals("FILLED")) {
        String amount = orderMap.get("executedQty").toString();
        String orderId = orderMap.get("orderId").toString();
        String symbol = orderMap.get("symbol").toString();
        String side = orderMap.get("side").toString();
        // String price = orderMap.get("price").toString();
        String price = Float.toString(fillsAvgPrice(order));
        return new Order(symbol, orderId, side, price, amount);
        // } else {
        // _logger.error("market buy should have filled .. why didnt it ??? " +
        // orderMap.toString());
        // }
      } else {
        _logger.error("Something wrong with buy orderMap");
        _logger.info(orderMap.toString());
      }

    } catch (Exception e) {
      _logger.error("createSellOrderMarket", e);
    }
    return null;
  }

  private boolean isOrderProperties(Map<String, Object> orderMap) {
    if (orderMap.containsKey("status") && orderMap.containsKey("executedQty")
        && orderMap.containsKey("price") && orderMap.containsKey("orderId") && orderMap.containsKey("symbol")
        && orderMap.containsKey("side")) {
      String dollars = orderMap.get("cummulativeQuoteQty").toString();
      if (NumberUtils.isParsable(dollars)) {
        return true;
      }

    }
    return false;
  }

  /**
   * Convert api response to Candlestick list. Current candle not included
   * 
   * @param str /api/v3/klines response
   * @return List<Candlestick>
   */
  private List<Candlestick> convertToCandleList(String str) {
    String[][] arr = Arrays.stream(str.substring(2, str.length() - 2).split("\\],\\["))
        .map(e -> Arrays.stream(e.replace("\"", "").split("\\s*,\\s*"))
            .toArray(String[]::new))
        .toArray(String[][]::new);
    List<Candlestick> candleList = new ArrayList<>();

    for (String[] a : arr) {
      if (a.length == 12) {
        Timestamp timestamp = new Timestamp(Long.parseLong(a[0]));
        candleList.add(new Candlestick(timestamp.toLocalDateTime(), Float.parseFloat(a[1]), Float.parseFloat(a[2]),
            Float.parseFloat(a[3]), Float.parseFloat(a[4]), Float.parseFloat(a[5])));
      } else {
        _logger.info("klines len not 12");
      }
    }
    
    return candleList;
  }

  private float fillsAvgPrice(String order) {
    float avgPrice = 0f;
    try {
      JSONObject jObj = new JSONObject(order);
      JSONArray ja_data = jObj.getJSONArray("fills");
      float totalQty = 0f;
      List<Fills> fillsList = new ArrayList<>();
      for (int i = 0; i < ja_data.length(); i++) {
        JSONObject jsonObj = ja_data.getJSONObject(i);
        Fills f = new Fills(jsonObj.getString("price"), jsonObj.getString("qty"));
        totalQty += f.getQty();
        fillsList.add(f);
      }
      for (int i = 0; i < fillsList.size(); i++) {
        avgPrice += fillsList.get(i).getQty() / totalQty * fillsList.get(i).getPrice();
      }
      System.out.println("avgPrice > " + avgPrice);
    } catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
    return avgPrice;
  }

  public void test() {

    LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
    parameters.put("timestamp", System.currentTimeMillis());
    _client.createTrade().account(parameters);
  
  }
}