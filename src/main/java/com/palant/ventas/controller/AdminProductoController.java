package com.palant.ventas.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.palant.ventas.model.dao.ProductoDao;
import com.palant.ventas.model.entidad.Cliente;
import com.palant.ventas.model.entidad.Producto;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {

    @Autowired
    private ProductoDao productoDao;

    // Check auth
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("adminLogueado") != null;
    }

    @GetMapping
    public String index(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("productos", productoDao.listarProductos());
        model.addAttribute("producto", new Producto());
        return "admin/productos";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto, 
                          @RequestParam("archivoImagen") MultipartFile archivoImagen, 
                          @RequestParam(value = "archivosExtra", required = false) List<MultipartFile> archivosExtra,
                          HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        
        try {
            if (!archivoImagen.isEmpty()) {
                String base64Image = Base64.getEncoder().encodeToString(archivoImagen.getBytes());
                String imageType = archivoImagen.getContentType();
                producto.setImagenBase64("data:" + imageType + ";base64," + base64Image);
            }
            
            int idProducto;
            if (producto.getIdProducto() == 0) {
                idProducto = productoDao.insertar(producto);
            } else {
                productoDao.actualizar(producto);
                idProducto = producto.getIdProducto();
            }

            if (archivosExtra != null) {
                for (MultipartFile extra : archivosExtra) {
                    if (!extra.isEmpty()) {
                        String base64Extra = Base64.getEncoder().encodeToString(extra.getBytes());
                        String typeExtra = extra.getContentType();
                        productoDao.insertarImagenExtra(idProducto, "data:" + typeExtra + ";base64," + base64Extra);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return "redirect:/admin/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") int id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        productoDao.eliminar(id);
        return "redirect:/admin/productos";
    }
}
