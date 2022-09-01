package su.ggoose.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IdPercentage {

  String clientId;
  String percentage;

  public IdPercentage(String clientId, String percentage) {
    this.clientId = clientId;
    this.percentage = percentage;
  }

}
