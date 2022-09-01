package su.ggoose.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "positions")
public class CoinPositionV1 {

  @Id
  private String id;
  private String orderId;
  private String amount;
  private String position;
  private float price;
  private String symbol;
  private boolean stopHit;
  private float stopPrice;
  private LocalDateTime date;
  private String stratName;
  @Transient
  private String stratNameTele = "v1";
  private List<IdPercentage> idPercentagesList;
  // @Transient
  // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy
  // HH:mm:ss");

  public CoinPositionV1(String position, String orderId, String amount, float price, String symbol,
      boolean stopHit,
      float stopPrice,
      String stratName,
      LocalDateTime date,
      List<IdPercentage> idPercentagesList) {
    this.amount = amount;
    this.orderId = orderId;
    this.position = position;
    this.price = price;
    this.symbol = symbol;
    this.stopHit = stopHit;
    this.stopPrice = stopPrice;
    this.date = date;
    this.stratName = stratName;
    this.idPercentagesList = idPercentagesList;
  }

  public String telegramMessage() {
    return this.position.equals("LONG")
        ? "{ " + this.symbol + "_" + this.stratNameTele + ", position: " + this.position + ", price: " + this.price
            + ", stop: "
            + this.stopPrice + ", date: " + this.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + " }"
        : "{ " + this.symbol + "_" + this.stratNameTele + ", position: " + this.position + ", price: " + this.price
            + ", date: " + this.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + " }";

  }
}
