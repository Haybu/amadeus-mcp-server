package io.agilehandy.amadeus_mcp_server;

import com.amadeus.Amadeus;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class AmadeusMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmadeusMcpServerApplication.class, args);
	}

    @Bean
    public Amadeus amadeus(@Value("${amadeus.client.id}") String clientId, @Value("${amadeus.client.secret}") String clientSecret) {
        return Amadeus.builder(Map.of("AMADEUS_CLIENT_ID", clientId, "AMADEUS_CLIENT_SECRET", clientSecret)).build();
    }

    @Bean
    public ToolCallbackProvider locationTools(AmadeusTools amadeusTools) {
        return MethodToolCallbackProvider.builder().toolObjects(amadeusTools).build();
    }

}
