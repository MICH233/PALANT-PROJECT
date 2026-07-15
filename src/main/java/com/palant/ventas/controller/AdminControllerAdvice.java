package com.palant.ventas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.palant.ventas.model.dao.PedidoDao;

@ControllerAdvice(assignableTypes = {
    AdminController.class, 
    AdminProductoController.class, 
    AdminBannerController.class
})
public class AdminControllerAdvice {

    @Autowired
    private PedidoDao pedidoDao;

    @ModelAttribute("pendientesCount")
    public int getPendientesCount() {
        return pedidoDao.contarPedidosPendientes();
    }
}
