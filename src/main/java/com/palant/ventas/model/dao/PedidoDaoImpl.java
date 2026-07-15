package com.palant.ventas.model.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.palant.ventas.model.entidad.Cliente;
import com.palant.ventas.model.entidad.ItemCarrito;
import com.palant.ventas.model.entidad.Pedido;

@Repository
public class PedidoDaoImpl implements PedidoDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    @Transactional
    public int procesarCompra(Cliente cliente, Pedido pedido, List<ItemCarrito> carrito) {
        // Ya no insertamos el cliente, usamos el ID del cliente logueado
        int idCliente = cliente.getIdCliente();

        // 1. Insertar Pedido y obtener ID
        int idPedido = template.execute(
            (Connection con) -> {
                CallableStatement cs = con.prepareCall("{call usp_insertar_pedido(?,?,?,?,?,?)}");
                cs.setInt(1, idCliente);
                cs.setDouble(2, pedido.getMontoTotal());
                cs.setString(3, pedido.getMetodoPago());
                cs.setString(4, pedido.getCodigoVoucher());
                cs.setString(5, pedido.getImagenVoucher());
                cs.registerOutParameter(6, Types.INTEGER);
                return cs;
            },
            (CallableStatement cs) -> {
                cs.execute();
                return cs.getInt(6);
            }
        );

        // 2. Insertar Detalles del Pedido
        for (ItemCarrito item : carrito) {
            template.update("CALL usp_insertar_detalle_pedido(?,?,?,?,?)",
                idPedido, item.getProducto().getIdProducto(), item.getCantidad(), item.getSubtotal(), item.getTalla());
        }

        return idPedido;
    }

    @Override
    public List<Pedido> listarPedidosVendedor() {
        return template.query("CALL usp_listar_pedidos_vendedor()", new RowMapper<Pedido>() {
            @Override
            public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {
                Pedido p = new Pedido();
                p.setIdPedido(rs.getInt("id_pedido"));
                p.setFecha(rs.getTimestamp("fecha"));
                p.setMontoTotal(rs.getDouble("monto_total"));
                p.setMetodoPago(rs.getString("metodo_pago"));
                p.setCodigoVoucher(rs.getString("codigo_voucher"));
                p.setImagenVoucher(rs.getString("imagen_voucher"));
                p.setEstado(rs.getString("estado"));
                
                Cliente c = new Cliente();
                c.setNombres(rs.getString("nombres"));
                c.setApellidos(rs.getString("apellidos"));
                c.setCelular(rs.getString("celular"));
                p.setCliente(c);
                return p;
            }
        });
    }

    @Override
    public List<Pedido> listarPedidosCliente(int idCliente) {
        return template.query("CALL usp_listar_pedidos_cliente(?)", new RowMapper<Pedido>() {
            @Override
            public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {
                Pedido p = new Pedido();
                p.setIdPedido(rs.getInt("id_pedido"));
                p.setFecha(rs.getTimestamp("fecha"));
                p.setMontoTotal(rs.getDouble("monto_total"));
                p.setMetodoPago(rs.getString("metodo_pago"));
                p.setCodigoVoucher(rs.getString("codigo_voucher"));
                p.setImagenVoucher(rs.getString("imagen_voucher"));
                p.setEstado(rs.getString("estado"));
                return p;
            }
        }, idCliente);
    }

    @Override
    public void validarPago(int idPedido, String estado) {
        template.update("CALL usp_validar_pago_pedido(?,?)", idPedido, estado);
    }

    @Override
    public int contarPedidosPendientes() {
        Integer count = template.queryForObject("SELECT COUNT(*) FROM pedido WHERE estado = 'Pendiente'", Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Pedido obtenerPedidoConDetalles(int idPedido) {
        List<Pedido> pedidos = template.query("SELECT p.*, c.nombres, c.apellidos, c.dni, c.celular FROM pedido p JOIN cliente c ON p.id_cliente = c.id_cliente WHERE p.id_pedido = ?", new RowMapper<Pedido>() {
            @Override
            public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {
                Pedido p = new Pedido();
                p.setIdPedido(rs.getInt("id_pedido"));
                p.setFecha(rs.getTimestamp("fecha"));
                p.setMontoTotal(rs.getDouble("monto_total"));
                p.setMetodoPago(rs.getString("metodo_pago"));
                p.setCodigoVoucher(rs.getString("codigo_voucher"));
                p.setImagenVoucher(rs.getString("imagen_voucher"));
                p.setEstado(rs.getString("estado"));
                
                Cliente c = new Cliente();
                c.setNombres(rs.getString("nombres"));
                c.setApellidos(rs.getString("apellidos"));
                c.setDni(rs.getString("dni"));
                c.setCelular(rs.getString("celular"));
                p.setCliente(c);
                return p;
            }
        }, idPedido);

        if (pedidos.isEmpty()) return null;
        Pedido p = pedidos.get(0);

        List<com.palant.ventas.model.entidad.DetallePedido> detalles = template.query("SELECT dp.id_detalle, dp.id_pedido, dp.id_producto, dp.cantidad, dp.subtotal, dp.talla, pr.nombre FROM detalle_pedido dp JOIN producto pr ON dp.id_producto = pr.id_producto WHERE dp.id_pedido = ?", new RowMapper<com.palant.ventas.model.entidad.DetallePedido>() {
            @Override
            public com.palant.ventas.model.entidad.DetallePedido mapRow(ResultSet rs, int rowNum) throws SQLException {
                com.palant.ventas.model.entidad.DetallePedido dp = new com.palant.ventas.model.entidad.DetallePedido();
                dp.setIdDetalle(rs.getInt("id_detalle"));
                dp.setIdPedido(rs.getInt("id_pedido"));
                dp.setIdProducto(rs.getInt("id_producto"));
                dp.setCantidad(rs.getInt("cantidad"));
                dp.setSubtotal(rs.getDouble("subtotal"));
                dp.setTalla(rs.getString("talla"));
                dp.setNombreProducto(rs.getString("nombre"));
                return dp;
            }
        }, idPedido);

        p.setDetalles(detalles);
        return p;
    }
}
