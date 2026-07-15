package com.palant.ventas.model.dao;
import com.palant.ventas.model.entidad.Cliente;

public interface UsuarioDao {
    Cliente login(String correo, String contrasena);
    void registrar(Cliente cliente);
    void actualizar(Cliente cliente); // FASE 4: Actualizar Perfil
}
