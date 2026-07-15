package com.palant.ventas.model.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.palant.ventas.model.entidad.Cliente;

@Repository
public class UsuarioDaoImpl implements UsuarioDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Cliente login(String correo, String contrasena) {
        String sql = "CALL usp_login_usuario(?, ?)";
        try {
            return template.queryForObject(sql, BeanPropertyRowMapper.newInstance(Cliente.class), correo, contrasena);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void registrar(Cliente cliente) {
        int id = template.execute(
            (Connection con) -> {
                CallableStatement cs = con.prepareCall("{call usp_insertar_cliente(?,?,?,?,?,?,?)}");
                cs.setString(1, cliente.getNombres());
                cs.setString(2, cliente.getApellidos());
                cs.setString(3, cliente.getDni());
                cs.setString(4, cliente.getCelular());
                cs.setString(5, cliente.getCorreo());
                cs.setString(6, cliente.getContrasena());
                cs.registerOutParameter(7, Types.INTEGER); // Now 7 parameters in phase 4 (foto is null)
                return cs;
            },
            (CallableStatement cs) -> {
                cs.execute();
                return cs.getInt(7);
            }
        );
        cliente.setIdCliente(id);
    }

    @Override
    public void actualizar(Cliente cliente) {
        String sql = "CALL usp_actualizar_cliente(?, ?, ?, ?, ?, ?)";
        template.update(sql, 
            cliente.getIdCliente(), 
            cliente.getNombres(), 
            cliente.getApellidos(), 
            cliente.getCelular(), 
            cliente.getContrasena(), 
            cliente.getFotoPerfil()
        );
    }
}
