package com.oxalio.auth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@SpringBootApplication
@RestController
public class Application {
  public static void main(String[] args){ SpringApplication.run(Application.class, args); }

  @PostMapping("/oauth/token")
  public Map<String,Object> token(){ return Map.of("access_token","demo-token","token_type","bearer"); }
}
