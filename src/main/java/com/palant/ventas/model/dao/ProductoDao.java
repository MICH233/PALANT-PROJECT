package com.palant.ventas.model.dao;

import java.util.List;
import com.palant.ventas.model.entidad.Producto;

import com.palant.ventas.model.entidad.ProductoImagen;

public interface ProductoDao {
    List<Producto> listarProductos();
    Producto obtenerPorId(int id);
    int insertar(Producto producto);
    void actualizar(Producto producto);
    void eliminar(int id);
    void insertarImagenExtra(int idProducto, String imagenBase64);
    List<ProductoImagen> listarImagenesProducto(int idProducto);
}
