package su.ggoose.Model;

import java.util.List;

import lombok.Getter;

@Getter
public class IndiTwo {
  List<Float> xxxList;
  List<Float> yyyList;

  public IndiTwo(List<Float> xxxList, List<Float> yyyList) {
    this.xxxList = xxxList;
    this.yyyList = yyyList;
  }
}
