package com.oxalio.invoice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1")
public class Application {
  public static void main(String[] args){ SpringApplication.run(Application.class, args); }

  @GetMapping("/invoices")
  public List<Map<String,String>> list(){ return List.of(Map.of("id","demo","status","PENDING")); }
}
