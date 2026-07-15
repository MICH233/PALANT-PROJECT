package com.palant.ventas.controller;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.palant.ventas.model.dao.PedidoDao;
import com.palant.ventas.model.entidad.DetallePedido;
import com.palant.ventas.model.entidad.Pedido;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class BoletaController {

    @Autowired
    private PedidoDao pedidoDao;

    @GetMapping("/boleta/{id}")
    public void descargarBoleta(@PathVariable("id") int id, HttpServletResponse response, HttpSession session) throws DocumentException, IOException {
        Pedido pedido = pedidoDao.obtenerPedidoConDetalles(id);
        
        if (pedido == null || !"Validado".equals(pedido.getEstado())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No se puede generar la boleta para este pedido.");
            return;
        }
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Boleta_PALANT_" + id + ".pdf");
        
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
        Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
        
        // Cabecera Empresa
        Paragraph titulo = new Paragraph("PALANT S.A.C.", fontHeader);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        
        Paragraph ruc = new Paragraph("RUC: 20123456789\nBoleta de Venta Electrónica", fontSub);
        ruc.setAlignment(Element.ALIGN_CENTER);
        ruc.setSpacingAfter(20);
        document.add(ruc);
        
        // Datos del Cliente y Factura
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        document.add(new Paragraph("N° de Pedido: 001-" + String.format("%06d", pedido.getIdPedido()), fontBold));
        document.add(new Paragraph("Fecha: " + sdf.format(pedido.getFecha()), fontNormal));
        document.add(new Paragraph("Cliente: " + pedido.getCliente().getNombres() + " " + pedido.getCliente().getApellidos(), fontNormal));
        document.add(new Paragraph("DNI: " + pedido.getCliente().getDni(), fontNormal));
        
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(10);
        document.add(espacio);
        
        // Tabla de Productos
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {1.5f, 4f, 1.5f, 1.5f, 1.5f});
        
        // Cabeceras Tabla
        String[] headers = {"Cant.", "Descripción", "Talla", "P. Unit", "Subtotal"};
        for(String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontBold));
            cell.setBackgroundColor(new Color(230, 230, 230));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        // Filas
        for (DetallePedido dp : pedido.getDetalles()) {
            PdfPCell cCant = new PdfPCell(new Phrase(String.valueOf(dp.getCantidad()), fontNormal));
            cCant.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cCant);
            
            table.addCell(new Phrase(dp.getNombreProducto(), fontNormal));
            
            PdfPCell cTalla = new PdfPCell(new Phrase(dp.getTalla() != null ? dp.getTalla() : "-", fontNormal));
            cTalla.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cTalla);
            
            PdfPCell cPUnit = new PdfPCell(new Phrase("S/ " + df.format(dp.getPrecioUnitario()), fontNormal));
            cPUnit.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cPUnit);
            
            PdfPCell cSub = new PdfPCell(new Phrase("S/ " + df.format(dp.getSubtotal()), fontNormal));
            cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cSub);
        }
        document.add(table);
        
        // Totales (IGV)
        double total = pedido.getMontoTotal();
        double subtotal = total / 1.18;
        double igv = total - subtotal;
        
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(10f);
        
        addTotalRow(totalsTable, "SUBTOTAL:", "S/ " + df.format(subtotal), fontNormal, fontNormal);
        addTotalRow(totalsTable, "IGV (18%):", "S/ " + df.format(igv), fontNormal, fontNormal);
        addTotalRow(totalsTable, "TOTAL:", "S/ " + df.format(total), fontBold, fontBold);
        
        document.add(totalsTable);
        
        document.close();
    }
    
    private void addTotalRow(PdfPTable table, String label, String value, Font fLabel, Font fValue) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, fLabel));
        cell1.setBorder(0);
        cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell1);
        
        PdfPCell cell2 = new PdfPCell(new Phrase(value, fValue));
        cell2.setBorder(0);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell2);
    }
}
