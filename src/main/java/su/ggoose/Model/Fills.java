package su.ggoose.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Fills {
  float price;
  float qty;
  // String commission;
  // String commissionAsset;
  // String tradeId;
  public Fills(String price, String qty) {
    this.price = Float.parseFloat(price);
    this.qty = Float.parseFloat(qty);
  }
}
