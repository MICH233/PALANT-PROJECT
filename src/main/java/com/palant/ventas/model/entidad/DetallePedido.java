package com.palant.ventas.model.entidad;

public class DetallePedido {
    private int idDetalle;
    private int idPedido;
    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private double subtotal;
    private String talla;

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
    
    public double getPrecioUnitario() {
        if(cantidad > 0) return subtotal / cantidad;
        return 0.0;
    }
}
