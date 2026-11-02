package br.jlturismo.voa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VoaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoaApplication.class, args);
    }
}
