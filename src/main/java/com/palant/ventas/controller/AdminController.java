package com.palant.ventas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.palant.ventas.model.dao.PedidoDao;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PedidoDao pedidoDao;

    // Check auth
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("adminLogueado") != null;
    }

    @GetMapping("/pedidos")
    public String verPedidos(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("pedidos", pedidoDao.listarPedidosVendedor());
        return "admin/pedidos";
    }

    @PostMapping("/pedidos/validar")
    public String validarPago(@RequestParam("idPedido") int idPedido, @RequestParam("accion") String accion, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        String estado = accion.equals("Aprobar") ? "Validado" : "Cancelado";
        pedidoDao.validarPago(idPedido, estado);
        return "redirect:/admin/pedidos";
    }
}
