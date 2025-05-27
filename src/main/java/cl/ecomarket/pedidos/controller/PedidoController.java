package cl.ecomarket.pedidos.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import cl.ecomarket.pedidos.client.InventarioClient;
import cl.ecomarket.pedidos.client.UsuarioClient;
import cl.ecomarket.pedidos.model.Pedido;
import cl.ecomarket.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final InventarioClient inventarioClient;
    private final UsuarioClient usuarioClient;

    @GetMapping("/verificar-stock")
    public boolean verificarStock(
            @RequestParam Long productoId,
            @RequestParam int cantidad
    ) {
        return inventarioClient.verificarStock(productoId, cantidad).block();
    }

    @GetMapping("/verificar-usuario")
    public boolean validarUsuario(
            @RequestParam String username 
    ) {
        return usuarioClient.validarUsuario(username).block();
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public Pedido crear(@RequestBody Pedido pedido) {
        return pedidoService.crearPedido(pedido);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR_SISTEMA', 'LOGISTICA')")
    public List<Pedido> listar() {
        return pedidoService.obtenerTodos();
    }


}