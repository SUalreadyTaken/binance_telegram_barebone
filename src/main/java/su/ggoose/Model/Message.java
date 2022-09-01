package su.ggoose.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

  private String chatId;
  private String text;

  public Message(String chatId, String text) {
    this.chatId = chatId;
    this.text = text;
  }
}
