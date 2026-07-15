package com.palant.ventas.model.dao;

import java.util.List;
import com.palant.ventas.model.entidad.Pedido;
import com.palant.ventas.model.entidad.Cliente;
import com.palant.ventas.model.entidad.ItemCarrito;

public interface PedidoDao {
    int procesarCompra(Cliente cliente, Pedido pedido, List<ItemCarrito> carrito);
    List<Pedido> listarPedidosVendedor();
    List<Pedido> listarPedidosCliente(int idCliente);
    void validarPago(int idPedido, String estado);
    int contarPedidosPendientes();
    Pedido obtenerPedidoConDetalles(int idPedido);
}
