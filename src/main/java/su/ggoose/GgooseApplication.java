package su.ggoose;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

@Controller
@SpringBootApplication
@EnableScheduling
public class GgooseApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(GgooseApplication.class, args);
  }

  @Override
  public void run(String... args) {

  }

}
