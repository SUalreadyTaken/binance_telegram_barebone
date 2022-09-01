package su.ggoose.Repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import su.ggoose.Model.History;

public interface HistoryRepository extends MongoRepository<History, String>{
  Slice<History> findByStratName(String name, Pageable pageable);
}
