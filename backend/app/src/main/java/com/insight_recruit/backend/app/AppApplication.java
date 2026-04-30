package com.insight_recruit.backend.app;

import com.insight_recruit.backend.app.config.RedisQueueProperties;
import com.insight_recruit.backend.app.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties({RedisQueueProperties.class, StorageProperties.class})
public class AppApplication {

	static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
	public static void main(String[] args) {


		SpringApplication.run(AppApplication.class, args);
	}

}
