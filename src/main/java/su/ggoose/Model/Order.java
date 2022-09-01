package su.ggoose.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Order {
  private String symbol;
  private String orderId;
  private String side; // dont actually need it can delete
  private String price;
  private String amount;

  public Order(String symbol, String orderId, String side, String price, String amount) {
    this.symbol = symbol;
    this.orderId = orderId;
    this.side = side;
    this.price = price;
    this.amount = amount;
  }

}
