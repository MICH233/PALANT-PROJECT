package com.palant.ventas.model.dao;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import com.palant.ventas.model.entidad.Producto;
import com.palant.ventas.model.entidad.ProductoImagen;

@Repository
public class ProductoDaoImpl implements ProductoDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public List<Producto> listarProductos() {
        String sql = "CALL usp_listar_productos()";
        return template.query(sql, BeanPropertyRowMapper.newInstance(Producto.class));
    }

    @Override
    public Producto obtenerPorId(int id) {
        String sql = "CALL usp_obtener_producto_por_id(?)";
        Producto p = template.queryForObject(sql, BeanPropertyRowMapper.newInstance(Producto.class), id);
        if (p != null) {
            p.setImagenesExtra(listarImagenesProducto(id));
        }
        return p;
    }

    @Override
    public int insertar(Producto p) {
        return template.execute(
            (Connection con) -> {
                CallableStatement cs = con.prepareCall("{call usp_insertar_producto(?,?,?,?,?,?,?,?)}");
                cs.setString(1, p.getNombre());
                cs.setString(2, p.getCategoria());
                cs.setString(3, p.getDescripcion());
                cs.setDouble(4, p.getPrecio());
                cs.setInt(5, p.getStock());
                cs.setString(6, p.getImagenBase64());
                cs.setString(7, p.getTallasDisponibles());
                cs.registerOutParameter(8, Types.INTEGER);
                return cs;
            },
            (CallableStatement cs) -> {
                cs.execute();
                return cs.getInt(8);
            }
        );
    }

    @Override
    public void actualizar(Producto p) {
        template.update("CALL usp_actualizar_producto(?,?,?,?,?,?,?,?)", 
            p.getIdProducto(), p.getNombre(), p.getCategoria(), p.getDescripcion(), p.getPrecio(), p.getStock(), p.getImagenBase64(), p.getTallasDisponibles());
    }

    @Override
    public void eliminar(int id) {
        template.update("CALL usp_eliminar_producto(?)", id);
    }

    @Override
    public void insertarImagenExtra(int idProducto, String imagenBase64) {
        template.update("CALL usp_insertar_producto_imagen(?,?)", idProducto, imagenBase64);
    }

    @Override
    public List<ProductoImagen> listarImagenesProducto(int idProducto) {
        String sql = "CALL usp_listar_imagenes_producto(?)";
        return template.query(sql, BeanPropertyRowMapper.newInstance(ProductoImagen.class), idProducto);
    }
}
