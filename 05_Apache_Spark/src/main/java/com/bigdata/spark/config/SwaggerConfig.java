package com.bigdata.spark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

/**
 * Configuration for OpenAPI 3.0 documentation using SpringDoc
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Apache Spark Big Data Processing API")
                        .description("Comprehensive REST API for Apache Spark operations including RDD, DataFrame, SQL, Streaming, ML, and GraphX")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Big Data Team")
                                .url("https://github.com/bigdata/spark-project")
                                .email("bigdata@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
