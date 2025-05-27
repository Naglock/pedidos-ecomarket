package cl.ecomarket.pedidos.service;

import cl.ecomarket.pedidos.client.InventarioClient;
import cl.ecomarket.pedidos.client.UsuarioClient;
import cl.ecomarket.pedidos.client.VentaClient;
import cl.ecomarket.pedidos.model.EstadoPedido;
import cl.ecomarket.pedidos.model.Pedido;
import cl.ecomarket.pedidos.model.ItemPedido;
import cl.ecomarket.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioClient usuarioClient;
    private final InventarioClient inventarioClient;
    private final VentaClient ventaClient;

    public Pedido crearPedido(Pedido pedido) {
        // 1. Validar que el cliente exista
        if (!usuarioClient.validarUsuario(pedido.getClienteUsername()).block()) {
            throw new RuntimeException("El cliente no existe: " + pedido.getClienteUsername());
        }

        // 2. Validar stock de cada item
        for (ItemPedido item : pedido.getItems()) {
            boolean hayStock = inventarioClient.verificarStock(item.getProductoId(), item.getCantidad()).block();
            if (!hayStock) {
                throw new RuntimeException("No hay stock suficiente para producto ID: " + item.getProductoId());
            }
            item.setPedido(pedido); // importante para mantener la relación bidireccional
        }

        // 3. Crear el pedido
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFechaCreacion(LocalDateTime.now());
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        Map<String, Object> ventaData = new HashMap<>();
        ventaData.put("clienteUsername", pedidoGuardado.getClienteUsername());
        ventaData.put("fechaCreacion", pedidoGuardado.getFechaCreacion().toString());

        List<Map<String, Object>> itemsMap = pedidoGuardado.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productoId", item.getProductoId());
                    itemMap.put("cantidad", item.getCantidad());
                    return itemMap;
                }).collect(Collectors.toList());

        ventaData.put("items", itemsMap);

        // 4. Crear la venta
        ventaClient.crearVenta(ventaData).block();

        return pedidoGuardado;
    }

    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }
}
