package su.ggoose;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import su.ggoose.Model.TeleClient;
import su.ggoose.Service.TeleClientService;

@Component
public class TeleClientList {
  private @Getter @Setter List<TeleClient> teleClientList = new ArrayList<>();
  private final TeleClientService teleClientService;

  public TeleClientList(TeleClientService teleClientService) {
    this.teleClientService = teleClientService;
  }

  @PostConstruct
  private void initChatIDs () {
    this.teleClientList = this.teleClientService.getTeleClients();
  }
  
}
