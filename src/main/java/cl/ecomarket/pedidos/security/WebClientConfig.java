package cl.ecomarket.pedidos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient inventarioWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081/api/v1/productos")
                .build();
    }

    @Bean
    public WebClient usuariosWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080/api/v1/usuarios")
                .build();
    }

    @Bean
    public WebClient ventasWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8082/api/v1/ventas")
                .build();
    }
}
