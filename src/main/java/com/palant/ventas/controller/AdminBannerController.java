package com.palant.ventas.controller;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.palant.ventas.model.dao.BannerDao;
import com.palant.ventas.model.entidad.Banner;

@Controller
@RequestMapping("/admin/banners")
public class AdminBannerController {

    @Autowired
    private BannerDao bannerDao;

    @GetMapping
    public String listarBanners(HttpSession session, Model model) {
        if (session.getAttribute("adminLogueado") == null) return "redirect:/admin/login";
        model.addAttribute("banners", bannerDao.listarBanners());
        return "admin/banners";
    }

    @PostMapping("/guardar")
    public String guardarBanner(@RequestParam("archivoBanner") MultipartFile archivoBanner, HttpSession session) {
        if (session.getAttribute("adminLogueado") == null) return "redirect:/admin/login";
        
        if (!archivoBanner.isEmpty()) {
            try {
                String base64Image = Base64.getEncoder().encodeToString(archivoBanner.getBytes());
                Banner b = new Banner();
                b.setImagenBase64("data:" + archivoBanner.getContentType() + ";base64," + base64Image);
                bannerDao.insertar(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "redirect:/admin/banners";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarBanner(@PathVariable("id") int id, HttpSession session) {
        if (session.getAttribute("adminLogueado") == null) return "redirect:/admin/login";
        bannerDao.eliminar(id);
        return "redirect:/admin/banners";
    }
}
