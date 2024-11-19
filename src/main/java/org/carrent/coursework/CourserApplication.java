package org.carrent.coursework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CourserApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourserApplication.class, args);
	}

}
