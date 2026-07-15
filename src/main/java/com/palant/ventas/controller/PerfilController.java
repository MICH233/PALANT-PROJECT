package com.palant.ventas.controller;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.palant.ventas.model.dao.UsuarioDao;
import com.palant.ventas.model.dao.PedidoDao;
import com.palant.ventas.model.entidad.Cliente;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioDao usuarioDao;
    
    @Autowired
    private PedidoDao pedidoDao;

    @GetMapping
    public String verPerfil(HttpSession session, Model model) {
        Cliente usuario = (Cliente) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("cliente", usuario);
        model.addAttribute("pedidos", pedidoDao.listarPedidosCliente(usuario.getIdCliente()));
        return "perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Cliente cliente, 
                                   @RequestParam(value="archivoFoto", required=false) MultipartFile archivoFoto, 
                                   HttpSession session) {
        Cliente usuarioActual = (Cliente) session.getAttribute("usuarioLogueado");
        if (usuarioActual == null) {
            return "redirect:/login";
        }

        // Mantener ID, DNI y correo intactos
        cliente.setIdCliente(usuarioActual.getIdCliente());
        cliente.setDni(usuarioActual.getDni());
        cliente.setCorreo(usuarioActual.getCorreo());

        // Manejar subida de foto
        if (archivoFoto != null && !archivoFoto.isEmpty()) {
            try {
                String base64Image = Base64.getEncoder().encodeToString(archivoFoto.getBytes());
                String imageType = archivoFoto.getContentType();
                cliente.setFotoPerfil("data:" + imageType + ";base64," + base64Image);
            } catch (IOException e) {
                e.printStackTrace();
                cliente.setFotoPerfil(usuarioActual.getFotoPerfil());
            }
        } else {
            // Si no sube foto, mantener la actual
            cliente.setFotoPerfil(usuarioActual.getFotoPerfil());
        }

        usuarioDao.actualizar(cliente);
        session.setAttribute("usuarioLogueado", cliente); // Actualizar sesión

        return "redirect:/perfil?exito=true";
    }
}
