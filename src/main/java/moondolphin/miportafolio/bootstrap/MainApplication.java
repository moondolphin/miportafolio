package moondolphin.miportafolio.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "moondolphin.miportafolio")
@EnableJpaRepositories(basePackages = "moondolphin.miportafolio")
@EntityScan(basePackages = "moondolphin.miportafolio")
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
