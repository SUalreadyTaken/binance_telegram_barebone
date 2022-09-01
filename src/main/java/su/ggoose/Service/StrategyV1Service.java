package su.ggoose.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import su.ggoose.Model.StrategyV1;
import su.ggoose.Repository.StrategyV1Repository;

@Service
public class StrategyV1Service {

  private final StrategyV1Repository strategyV1Repository;

  public StrategyV1Service(StrategyV1Repository strategyV1Repository) {
    this.strategyV1Repository = strategyV1Repository;
  }

  public Map<String, StrategyV1> getAll() {
    List<StrategyV1> tmp = this.strategyV1Repository.findAll();
    Map<String, StrategyV1> map = new HashMap<>();
    for(StrategyV1 s : tmp) {
      map.put(s.getStratName(), s);
    }
    return map;
  }
  
}
