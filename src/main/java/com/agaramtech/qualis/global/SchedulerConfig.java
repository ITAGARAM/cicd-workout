package com.agaramtech.qualis.global;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.io.InputStream;
import jakarta.annotation.PostConstruct;

@Configuration
@PropertySource(
{
	"classpath:javaScheduler.properties",
    "classpath:databaseCleaner.properties"
})
public class SchedulerConfig {
}
