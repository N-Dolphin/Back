package org.example.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
@ConfigurationPropertiesScan // class path 존재하는 모든 ConfigurationProperties Scan(필수❗️)

public class BackApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}

}
