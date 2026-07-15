package com.palant.ventas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.palant.ventas.model.dao.VendedorDao;
import com.palant.ventas.model.entidad.Vendedor;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private VendedorDao vendedorDao;

    @GetMapping("/login")
    public String verLoginAdmin() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String procesarLoginAdmin(@RequestParam("correo") String correo, 
                                     @RequestParam("contrasena") String contrasena, 
                                     HttpSession session, Model model) {
        Vendedor admin = vendedorDao.login(correo, contrasena);
        if (admin != null) {
            session.setAttribute("adminLogueado", admin);
            return "redirect:/admin/productos";
        }
        model.addAttribute("error", "Credenciales administrativas incorrectas");
        return "admin/login";
    }

    @GetMapping("/logout")
    public String logoutAdmin(HttpSession session) {
        session.removeAttribute("adminLogueado");
        return "redirect:/admin/login";
    }
}
