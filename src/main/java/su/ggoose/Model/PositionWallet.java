package su.ggoose.Model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionWallet {
  List<Client> clientList;

  public PositionWallet(List<Client> clientList) {
    this.clientList = clientList;
  }
}
