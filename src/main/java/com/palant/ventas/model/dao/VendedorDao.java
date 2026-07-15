package com.palant.ventas.model.dao;
import com.palant.ventas.model.entidad.Vendedor;

public interface VendedorDao {
    Vendedor login(String correo, String contrasena);
}
