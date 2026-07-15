package com.palant.ventas.model.dao;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.palant.ventas.model.entidad.Banner;

@Repository
public class BannerDaoImpl implements BannerDao {

    @Autowired
    private JdbcTemplate template;

    @Override
    public void insertar(Banner banner) {
        String sql = "CALL usp_insertar_banner(?)";
        template.update(sql, banner.getImagenBase64());
    }

    @Override
    public List<Banner> listarBanners() {
        String sql = "CALL usp_listar_banners()";
        return template.query(sql, BeanPropertyRowMapper.newInstance(Banner.class));
    }

    @Override
    public void eliminar(int id) {
        String sql = "CALL usp_eliminar_banner(?)";
        template.update(sql, id);
    }
}
