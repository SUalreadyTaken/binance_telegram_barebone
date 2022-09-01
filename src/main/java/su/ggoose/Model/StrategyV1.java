package su.ggoose.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;

@Getter
@Document(collection = "strategy_v1")
public class StrategyV1 {

  @Id
  private String id;
  private String stratName;
  private String symbolName;
  // FIXME
  /*
   * ## REMOVED TRADE SECRET ##
   */

  public StrategyV1(String stratName, String symbolName) {
    this.stratName = stratName;
    this.symbolName = symbolName;
  }

}
