package cl.ecomarket.pedidos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Service
public class InventarioClient {

    private static final Logger logger = LoggerFactory.getLogger(InventarioClient.class);

    private final WebClient inventarioWebClient;

    public InventarioClient(@Qualifier("inventarioWebClient") WebClient inventarioWebClient) {
        this.inventarioWebClient = inventarioWebClient.mutate()
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

    public Mono<Boolean> verificarStock(Long productoId, int cantidadSolicitada) {
        logger.info("Llamando a microservicio de inventario para verificar stock del producto {} con cantidad {}", productoId, cantidadSolicitada);
        
        return inventarioWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/{productoId}/stock")
                .queryParam("stock", cantidadSolicitada)
                .build(productoId))
            .retrieve()
            .bodyToMono(Boolean.class)
            .doOnNext(result -> logger.info("Resultado de stock para producto {}: {}", productoId, result))
            .doOnError(error -> logger.error("Error al verificar stock para producto {}: {}", productoId, error.getMessage()));
    }
}
