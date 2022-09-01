package su.ggoose.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import su.ggoose.Model.TeleClient;

public interface TeleClientRepository extends MongoRepository<TeleClient, String>{
  
}
