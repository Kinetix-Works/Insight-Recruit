package com.insight_recruit.backend.app;

import com.insight_recruit.backend.app.config.RedisQueueProperties;
import com.insight_recruit.backend.app.config.StorageProperties;
import com.insight_recruit.backend.app.config.LlmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties({RedisQueueProperties.class, StorageProperties.class, LlmProperties.class})
@EnableAsync
public class AppApplication {

	static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
	public static void main(String[] args) {


		SpringApplication.run(AppApplication.class, args);
	}

}
