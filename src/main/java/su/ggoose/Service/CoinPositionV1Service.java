package su.ggoose.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import su.ggoose.Model.CoinPositionV1;
import su.ggoose.Model.StrategyV1;
import su.ggoose.Repository.CoinPositionV1Repository;

@Service
public class CoinPositionV1Service {

  private final CoinPositionV1Repository coinPositionV1Repository;

  public CoinPositionV1Service(CoinPositionV1Repository coinPositionV1Repository) {
    this.coinPositionV1Repository = coinPositionV1Repository;
  }

  public Map<String, CoinPositionV1> getLatestV1s(Map<String, StrategyV1> strategyV1Map) {
    List<CoinPositionV1> latestList = new ArrayList<>();
    Map<String, CoinPositionV1> result = new HashMap<>();
    strategyV1Map.entrySet().forEach(strat -> this.coinPositionV1Repository.findFirstByStratNameOrderByIdDesc(strat.getKey())
        .ifPresent(cp -> latestList.add(cp)));
    for(CoinPositionV1 cp: latestList) {
      result.put(cp.getStratName(), cp);
    }
    return result;
  }

  public CoinPositionV1 insert(CoinPositionV1 coinPositionV1) {
    return this.coinPositionV1Repository.insert(coinPositionV1);
  }

  public CoinPositionV1 save(CoinPositionV1 coinPositionV1) {
    return this.coinPositionV1Repository.save(coinPositionV1);
  }
  
}
