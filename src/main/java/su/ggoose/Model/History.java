package su.ggoose.Model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "history")
public class History {
  String stratName;
  float open;
  float close;
  LocalDateTime openDate;
  LocalDateTime closeDate;
  PositionWallet positionWallet;
  float wallet;
  
  public History(String stratName, float open, float close, LocalDateTime openDate, LocalDateTime closeDate, PositionWallet positionWallet, float wallet) {
    this.stratName = stratName;
    this.open = open;
    this.close = close;
    this.openDate = openDate;
    this.closeDate = closeDate;
    this.positionWallet = positionWallet;
    this.wallet = wallet;
  }
}
