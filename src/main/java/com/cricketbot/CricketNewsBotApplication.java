package com.cricketbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  
public class CricketNewsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CricketNewsBotApplication.class, args);
	}

}
