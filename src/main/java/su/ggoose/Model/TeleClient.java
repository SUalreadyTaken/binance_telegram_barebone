package su.ggoose.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;

@Getter
@Document(collection = "tele_clients")
public class TeleClient {

  @Getter
  public class StrategyClient {
    private String name;
    // private String symbol;
    private String percentage;

    public StrategyClient(String name, String percentage) {
      this.name = name;
      // this.symbol = symbol;
      this.percentage = percentage;
    }
  }

  @Id
  private String id;
  private String chatId;
  private List<StrategyClient> strategies;
  private int infoLevel;
  private LocalDateTime regDate;

  public TeleClient(String chatId, int infoLevel, LocalDateTime regDate) {
    this.chatId = chatId;
    this.infoLevel = infoLevel;
    this.regDate = regDate;
    this.strategies = new ArrayList<>();
  }

  public void addStrategy(String name, String percentage) {
    this.strategies.add(new StrategyClient(name, percentage));
  }

}
