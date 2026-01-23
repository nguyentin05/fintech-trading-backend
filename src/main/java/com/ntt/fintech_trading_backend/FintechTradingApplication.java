package com.ntt.fintech_trading_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FintechTradingApplication {
	public static void main(String[] args) {
		SpringApplication.run(FintechTradingApplication.class, args);
	}
}
