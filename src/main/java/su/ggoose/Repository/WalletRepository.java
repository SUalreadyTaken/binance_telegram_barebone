package su.ggoose.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import su.ggoose.Model.Wallet;

public interface WalletRepository extends MongoRepository<Wallet, String>{

  Optional<Wallet> findByStrategy(String strat);
  
}
