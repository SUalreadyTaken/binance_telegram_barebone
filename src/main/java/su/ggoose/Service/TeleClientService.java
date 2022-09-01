package su.ggoose.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import su.ggoose.Model.TeleClient;
import su.ggoose.Repository.TeleClientRepository;

@Service
public class TeleClientService {

  private final TeleClientRepository teleClientRepository;

  public TeleClientService(TeleClientRepository teleClientRepository) {
    this.teleClientRepository = teleClientRepository;
  }

  public List<TeleClient> getTeleClients() {
    return teleClientRepository.findAll();
  }

  public void insert(TeleClient teleClient) {
    this.teleClientRepository.insert(teleClient);
  }
  
}
