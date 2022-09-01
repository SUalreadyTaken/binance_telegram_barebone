package su.ggoose.Model;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@NoArgsConstructor
@Getter
@Setter
@Component
public class MessageToSend {
  private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
}
