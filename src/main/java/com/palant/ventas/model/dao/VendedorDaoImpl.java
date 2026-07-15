package com.palant.ventas.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.palant.ventas.model.entidad.Vendedor;

@Repository
public class VendedorDaoImpl implements VendedorDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public Vendedor login(String correo, String contrasena) {
        String sql = "CALL usp_login_vendedor(?, ?)";
        try {
            return template.queryForObject(sql, BeanPropertyRowMapper.newInstance(Vendedor.class), correo, contrasena);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
