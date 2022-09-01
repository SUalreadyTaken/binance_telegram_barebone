package su.ggoose.Model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Candlestick {
  private LocalDateTime date;
  private float open;
  private float high;
  private float low;
  private float close;
  private float volume;


  public Candlestick() {
  }

  public Candlestick(LocalDateTime date, float open, float high, float low, float close, float volume) {
      this.date = date;
      this.open = open;
      this.high = high;
      this.low = low;
      this.close = close;
      this.volume = volume;
  }
}