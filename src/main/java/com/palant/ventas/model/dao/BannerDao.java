package com.palant.ventas.model.dao;
import java.util.List;
import com.palant.ventas.model.entidad.Banner;

public interface BannerDao {
    void insertar(Banner banner);
    List<Banner> listarBanners();
    void eliminar(int id);
}
