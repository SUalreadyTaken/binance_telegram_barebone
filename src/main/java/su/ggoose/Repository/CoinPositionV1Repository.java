package su.ggoose.Repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import su.ggoose.Model.CoinPositionV1;

public interface CoinPositionV1Repository extends MongoRepository<CoinPositionV1, String> {
  Optional<CoinPositionV1> findFirstByStratNameOrderByIdDesc(String name);
}
