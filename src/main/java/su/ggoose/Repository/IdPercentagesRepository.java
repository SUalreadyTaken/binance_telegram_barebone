package su.ggoose.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import su.ggoose.Model.IdPercentage;

public interface IdPercentagesRepository extends MongoRepository<IdPercentage, String> {
}
