package it.korea.app_bmpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AppBmpcApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppBmpcApplication.class, args);
	}
}
