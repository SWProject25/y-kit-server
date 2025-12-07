package com.twojz.y_kit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class YKitApplication {

	public static void main(String[] args) {
		SpringApplication.run(YKitApplication.class, args);
	}

}
