package su.ggoose.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import su.ggoose.Model.Wallet;
import su.ggoose.Repository.WalletRepository;

@Service
public class WalletService {
  
  private final WalletRepository walletRepository;

  public WalletService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public Wallet getWallet(String strat) {
    Optional<Wallet> wallet = walletRepository.findByStrategy(strat);
    return wallet.isPresent() ? wallet.get() : null;
  }

  public void save(Wallet wallet) {
    walletRepository.save(wallet);
  }
}
