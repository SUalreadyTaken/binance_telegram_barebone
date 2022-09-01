package su.ggoose.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import su.ggoose.Model.History;
import su.ggoose.Repository.HistoryRepository;

@Service
public class HistoryService {
  private final HistoryRepository historyRepository;

  public HistoryService(HistoryRepository historyRepository) {
    this.historyRepository = historyRepository;
  }

  public List<History> getAllHistory(String stratName, int lookback) {
    Pageable sortedByDate = PageRequest.of(0, lookback, Sort.by("openDate").descending());
    List<History> inCollection = new ArrayList<>(
        historyRepository.findByStratName(stratName, sortedByDate).getContent());
    return inCollection;
  }
}
