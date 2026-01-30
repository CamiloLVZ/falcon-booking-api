package com.falcon.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FalconBookingSystemApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FalconBookingSystemApiApplication.class, args);
	}

}
