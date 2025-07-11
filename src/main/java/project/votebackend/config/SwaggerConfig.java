package project.votebackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        Server prodServer = new Server()
                .url("https://votey-backend.p-e.kr")
                .description("Production Server");

        return new OpenAPI()
                .info(new Info()
                        .title("Votey API")
                        .version("v1")
                        .description("Votey 백엔드 API 명세서입니다."))
                .servers(List.of(prodServer));
    }
}
