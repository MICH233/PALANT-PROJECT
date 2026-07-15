package com.palant.ventas.model.entidad;

import java.util.Date;

public class Pedido {
    private int idPedido;
    private int idCliente;
    private Date fecha;
    private double montoTotal;
    private String metodoPago;
    private String codigoVoucher;
    private String imagenVoucher; // FASE 4: Voucher
    private String estado;
    
    // For joining with Cliente in Admin view
    private Cliente cliente;
    
    // For joining with details for Boleta
    private java.util.List<DetallePedido> detalles = new java.util.ArrayList<>();

    public java.util.List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(java.util.List<DetallePedido> detalles) { this.detalles = detalles; }

    public String getImagenVoucher() { return imagenVoucher; }
    public void setImagenVoucher(String imagenVoucher) { this.imagenVoucher = imagenVoucher; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getCodigoVoucher() { return codigoVoucher; }
    public void setCodigoVoucher(String codigoVoucher) { this.codigoVoucher = codigoVoucher; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}
