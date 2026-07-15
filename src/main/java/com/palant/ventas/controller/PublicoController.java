package com.palant.ventas.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.palant.ventas.model.dao.PedidoDao;
import com.palant.ventas.model.dao.ProductoDao;
import com.palant.ventas.model.entidad.*;

@Controller
@RequestMapping("/")
public class PublicoController {

    @Autowired
    private ProductoDao productoDao;
    @Autowired
    private PedidoDao pedidoDao;
    @Autowired
    private com.palant.ventas.model.dao.BannerDao bannerDao;

    @GetMapping({"/", "/catalogo"})
    public String catalogo(Model model) {
        model.addAttribute("productos", productoDao.listarProductos());
        model.addAttribute("banners", bannerDao.listarBanners());
        return "catalogo";
    }

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable("id") int id, Model model) {
        Producto p = productoDao.obtenerPorId(id);
        if (p == null) {
            return "redirect:/catalogo";
        }
        model.addAttribute("producto", p);
        return "producto_detalle";
    }

    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(@RequestParam("id") int idProducto, @RequestParam("cant") int cant, @RequestParam(value="talla", required=false) String talla, HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }
        
        Producto p = productoDao.obtenerPorId(idProducto);
        if (p != null && p.getStock() >= cant) {
            boolean existe = false;
            for (ItemCarrito item : carrito) {
                boolean idMatch = item.getProducto().getIdProducto() == idProducto;
                boolean tallaMatch = (talla == null && item.getTalla() == null) || (talla != null && talla.equals(item.getTalla()));
                
                if (idMatch && tallaMatch) {
                    item.setCantidad(item.getCantidad() + cant);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                carrito.add(new ItemCarrito(p, cant, talla));
            }
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/carrito";
    }

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        double total = 0;
        if (carrito != null) {
            for (ItemCarrito item : carrito) total += item.getSubtotal();
        }
        model.addAttribute("total", total);
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("pedido", new Pedido());
        return "carrito";
    }

    @PostMapping("/carrito/actualizar")
    public String actualizarCarrito(@RequestParam("id") int idProducto, @RequestParam("cant") int cant, @RequestParam(value="talla", required=false) String talla, HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito != null) {
            for (ItemCarrito item : carrito) {
                boolean idMatch = item.getProducto().getIdProducto() == idProducto;
                boolean tallaMatch = (talla == null && item.getTalla() == null) || (talla != null && talla.equals(item.getTalla()));
                
                if (idMatch && tallaMatch) {
                    if (cant > 0) item.setCantidad(cant);
                    break;
                }
            }
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/eliminar")
    public String eliminarDelCarrito(@RequestParam("id") int idProducto, @RequestParam(value="talla", required=false) String talla, HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito != null) {
            carrito.removeIf(item -> {
                boolean idMatch = item.getProducto().getIdProducto() == idProducto;
                boolean tallaMatch = (talla == null && item.getTalla() == null) || (talla != null && talla.equals(item.getTalla()));
                return idMatch && tallaMatch;
            });
            session.setAttribute("carrito", carrito);
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/checkout")
    public String procesarCheckout(@ModelAttribute Pedido pedido, 
                                   @RequestParam(value="archivoVoucher", required=false) MultipartFile archivoVoucher,
                                   HttpSession session) {
        Cliente usuarioLogueado = (Cliente) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/login";
        }
        
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito != null && !carrito.isEmpty()) {
            double total = 0;
            for (ItemCarrito item : carrito) total += item.getSubtotal();
            pedido.setMontoTotal(total);

            if (archivoVoucher != null && !archivoVoucher.isEmpty()) {
                try {
                    String base64Image = Base64.getEncoder().encodeToString(archivoVoucher.getBytes());
                    pedido.setImagenVoucher("data:" + archivoVoucher.getContentType() + ";base64," + base64Image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            pedidoDao.procesarCompra(usuarioLogueado, pedido, carrito);
            session.removeAttribute("carrito");
            return "redirect:/catalogo?exito=true";
        }
        return "redirect:/carrito";
    }
}
