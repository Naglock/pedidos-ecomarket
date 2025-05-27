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
public class UsuarioClient {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioClient.class);


    private final WebClient usuariosWebClient;

    public UsuarioClient(@Qualifier("usuariosWebClient") WebClient usuariosWebClient) {
        this.usuariosWebClient = usuariosWebClient.mutate()
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

    public Mono<Boolean> validarUsuario(String username) {
        logger.info("Llamando a microservicio de usuarios para validar usuario: {}", username);
        
        return usuariosWebClient.get()
                .uri("/existe/{username}", username)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnNext(result -> logger.info("¿Usuario {} válido? {}", username, result))
                .doOnError(error -> logger.error("Error al validar usuario {}: {}", username, error.getMessage()));

    }
}
