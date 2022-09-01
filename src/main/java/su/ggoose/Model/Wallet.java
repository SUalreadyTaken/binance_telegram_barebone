package su.ggoose.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "wallet")
public class Wallet {

  @Id
  private String id;
  @Indexed(unique=true)
  private String strategy;
  private String symbol;
  private String cash;


  public Wallet(String strategy, String symbol, String cash) {
    this.strategy = strategy;
    this.symbol = symbol;
    this.cash = cash;
  }
  
}
