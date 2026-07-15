package com.palant.ventas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.palant.ventas.model.dao.UsuarioDao;
import com.palant.ventas.model.entidad.Cliente;

@Controller
public class LoginController {

    @Autowired
    private UsuarioDao usuarioDao;

    @GetMapping("/login")
    public String verLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("correo") String correo, 
                                @RequestParam("contrasena") String contrasena, 
                                HttpSession session, Model model) {
        Cliente usuario = usuarioDao.login(correo, contrasena);
        if (usuario != null) {
            session.setAttribute("usuarioLogueado", usuario);
            
            // Si tiene items en el carrito, mandarlo a pagar en vez de al catálogo
            java.util.List<?> carrito = (java.util.List<?>) session.getAttribute("carrito");
            if (carrito != null && !carrito.isEmpty()) {
                return "redirect:/carrito";
            }
            return "redirect:/catalogo";
        }
        model.addAttribute("error", "Credenciales incorrectas");
        return "login";
    }

    @GetMapping("/registro")
    public String verRegistro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Cliente cliente) {
        usuarioDao.registrar(cliente);
        return "redirect:/login?registrado=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
