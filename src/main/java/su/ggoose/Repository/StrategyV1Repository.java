package su.ggoose.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import su.ggoose.Model.StrategyV1;

public interface StrategyV1Repository extends MongoRepository<StrategyV1, String>{
  
}
