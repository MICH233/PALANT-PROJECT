package com.palant.ventas.model.entidad;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;
    private String talla;

    public ItemCarrito(Producto producto, int cantidad, String talla) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.talla = talla;
    }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
    public double getSubtotal() { return this.producto.getPrecio() * this.cantidad; }
}
