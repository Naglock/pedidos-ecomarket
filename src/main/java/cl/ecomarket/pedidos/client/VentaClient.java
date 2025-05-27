package cl.ecomarket.pedidos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

@Service
public class VentaClient {

    private static final Logger logger = LoggerFactory.getLogger(VentaClient.class);


    private final WebClient ventasWebClient;

    public VentaClient(@Qualifier("ventasWebClient") WebClient ventasWebClient) {
        this.ventasWebClient = ventasWebClient.mutate()
            .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                String token = obtenerTokenJwt();
                if (token != null) {
                    ClientRequest newRequest = ClientRequest.from(clientRequest)
                        .header("Authorization", "Bearer " + token)
                        .build();
                    return Mono.just(newRequest);
                }
                return Mono.just(clientRequest);
            }))
            .build();
    }

    private String obtenerTokenJwt() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }

    public Mono<Map<String, Object>> crearVenta(Map<String, Object> ventaData) {
        logger.info("Llamando a microservicio de ventas para crear venta con data: {}", ventaData);

        return ventasWebClient.post()
                .uri("/")
                .bodyValue(ventaData)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(result -> logger.info("Venta creada con respuesta: {}", result))
                .doOnError(error -> logger.error("Error al crear venta: {}", error.getMessage()));

    }
}
