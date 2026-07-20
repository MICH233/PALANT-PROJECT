package com.palant.ventas.model.entidad;

import java.util.List;
import java.util.ArrayList;

public class Producto {
    private int idProducto;
    private String nombre;
    private String categoria;
    private String descripcion;
    private double precio;
    private int stock;
    private String imagenBase64;
    private List<ProductoImagen> imagenesExtra = new ArrayList<>();
    private String tallasDisponibles;
    private String estado;

    // Getters and Setters
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
    
    public List<ProductoImagen> getImagenesExtra() { return imagenesExtra; }
    public void setImagenesExtra(List<ProductoImagen> imagenesExtra) { this.imagenesExtra = imagenesExtra; }
    
    public String getTallasDisponibles() { return tallasDisponibles; }
    public void setTallasDisponibles(String tallasDisponibles) { this.tallasDisponibles = tallasDisponibles; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
