package su.ggoose.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client {
  String id;
  float percentage;

  public Client(String id, float percentage) {
    this.id = id;
    this.percentage = percentage;
  }

}
