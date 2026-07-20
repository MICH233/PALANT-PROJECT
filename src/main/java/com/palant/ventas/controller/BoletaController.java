package com.palant.ventas.controller;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
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

    // ---------- Paleta de colores (colores de marca: verde encendido + azul
    // marino) ----------
    private static final Color COLOR_TEXTO = new Color(30, 30, 30);
    private static final Color COLOR_GRIS_BG = new Color(235, 242, 238); // fondo cabecera de tabla (tinte verdoso
                                                                         // claro)
    private static final Color COLOR_BORDE = new Color(0, 42, 92); // azul marino — bordes de las cajas
    private static final Color COLOR_GRIS_TXT = new Color(100, 100, 100);
    private static final Color COLOR_ACENTO = new Color(0, 168, 89); // verde encendido — nombre de marca
    private static final Color COLOR_MARINO = new Color(0, 42, 92); // azul marino — textos de acento

    @GetMapping("/boleta/{id}")
    public void descargarBoleta(@PathVariable("id") int id, HttpServletResponse response, HttpSession session)
            throws DocumentException, IOException {
        Pedido pedido = pedidoDao.obtenerPedidoConDetalles(id);

        if (pedido == null || !"Validado".equals(pedido.getEstado())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No se puede generar la boleta para este pedido.");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Boleta_PALANT_" + id + ".pdf");

        Document document = new Document(PageSize.A4, 36, 36, 30, 30);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // ---------- Fuentes ----------
        Font fontMarca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_ACENTO);
        Font fontMarcaResto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_MARINO);
        Font fontTagline = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, COLOR_GRIS_TXT);
        Font fontRazonSocial = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_MARINO);
        Font fontDatosEmp = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_GRIS_TXT);
        Font fontRucBox = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_MARINO);
        Font fontTipoDoc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_MARINO);
        Font fontNumBox = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_MARINO);
        Font fontLabelBox = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_MARINO);
        Font fontValorBox = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXTO);
        Font fontTablaHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_MARINO);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXTO);
        Font fontTotalLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_MARINO);
        Font fontImporteLet = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_MARINO);
        Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 8, COLOR_GRIS_TXT);

        DecimalFormat df = new DecimalFormat("#,##0.00");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // ======================================================
        // 1) CABECERA: logo + datos empresa (izq) | caja RUC/tipo doc (der)
        // ======================================================
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[] { 62f, 38f });

        PdfPCell celdaEmpresa = new PdfPCell();
        celdaEmpresa.setBorder(0);
        celdaEmpresa.setPaddingRight(12);

        Image logo = cargarLogo();
        if (logo != null) {
            logo.scaleToFit(100, 55);
            celdaEmpresa.addElement(logo);
        } else {
            Paragraph marca = new Paragraph();
            marca.add(new Chunk("PALANT", fontMarca));
            marca.add(new Chunk(" S.A.C.", fontMarcaResto));
            celdaEmpresa.addElement(marca);
        }

        Paragraph tagline = new Paragraph("Moda deportiva y casual", fontTagline);
        tagline.setSpacingBefore(1f);
        tagline.setSpacingAfter(6f);
        celdaEmpresa.addElement(tagline);

        Paragraph razonSocial = new Paragraph("PALANT SOCIEDAD ANÓNIMA CERRADA", fontRazonSocial);
        celdaEmpresa.addElement(razonSocial);

        Paragraph datosEmpresa = new Paragraph();
        datosEmpresa.setFont(fontDatosEmp);
        datosEmpresa.setLeading(11f);
        datosEmpresa.setSpacingBefore(2f);
        datosEmpresa.add("Av. Principal 123, Lima - Perú\n");
        datosEmpresa.add("Celular ventas: 900 000 000 - Email: ventas@palant.com");
        celdaEmpresa.addElement(datosEmpresa);
        header.addCell(celdaEmpresa);

        // --- Celda derecha: caja con borde (RUC + tipo doc + número) ---
        PdfPCell cajaDerecha = new PdfPCell();
        cajaDerecha.setBorder(PdfPCell.BOX);
        cajaDerecha.setBorderColor(COLOR_BORDE);
        cajaDerecha.setBorderWidth(1.3f);
        cajaDerecha.setPadding(10);
        cajaDerecha.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph pRucBox = new Paragraph("RUC: 20123456789", fontRucBox);
        pRucBox.setAlignment(Element.ALIGN_CENTER);
        cajaDerecha.addElement(pRucBox);

        Paragraph pTipoDoc = new Paragraph("BOLETA DE VENTA\nELECTRÓNICA", fontTipoDoc);
        pTipoDoc.setAlignment(Element.ALIGN_CENTER);
        pTipoDoc.setSpacingBefore(4f);
        cajaDerecha.addElement(pTipoDoc);

        Paragraph pNumBox = new Paragraph("B001-" + String.format("%06d", pedido.getIdPedido()), fontNumBox);
        pNumBox.setAlignment(Element.ALIGN_CENTER);
        pNumBox.setSpacingBefore(4f);
        cajaDerecha.addElement(pNumBox);

        header.addCell(cajaDerecha);
        document.add(header);

        Paragraph espacio1 = new Paragraph(" ");
        espacio1.setSpacingAfter(10);
        document.add(espacio1);

        // ======================================================
        // 2) DATOS DEL CLIENTE (izq) | FECHA / MONEDA (der) — en cajas
        // ======================================================
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 60f, 40f });
        infoTable.setSpacingAfter(12f);

        PdfPCell celdaCliente = new PdfPCell();
        celdaCliente.setBorder(PdfPCell.BOX);
        celdaCliente.setBorderColor(COLOR_BORDE);
        celdaCliente.setPadding(9);

        Paragraph tituloCliente = new Paragraph("DATOS DEL CLIENTE", fontLabelBox);
        tituloCliente.setSpacingAfter(4f);
        celdaCliente.addElement(tituloCliente);
        celdaCliente.addElement(filaLabelValor("DNI", pedido.getCliente().getDni(), fontLabelBox, fontValorBox));
        celdaCliente.addElement(
                filaLabelValor("CLIENTE", pedido.getCliente().getNombres() + " " + pedido.getCliente().getApellidos(),
                        fontLabelBox, fontValorBox));
        infoTable.addCell(celdaCliente);

        PdfPCell celdaFecha = new PdfPCell();
        celdaFecha.setBorder(PdfPCell.BOX);
        celdaFecha.setBorderColor(COLOR_BORDE);
        celdaFecha.setPadding(9);

        Paragraph tituloFecha = new Paragraph(" ", fontLabelBox);
        tituloFecha.setSpacingAfter(4f);
        celdaFecha.addElement(tituloFecha);
        celdaFecha
                .addElement(filaLabelValor("FECHA EMISIÓN", sdf.format(pedido.getFecha()), fontLabelBox, fontValorBox));
        celdaFecha.addElement(filaLabelValor("MONEDA", "SOLES", fontLabelBox, fontValorBox));
        infoTable.addCell(celdaFecha);

        document.add(infoTable);

        // ======================================================
        // 3) TABLA DE PRODUCTOS
        // ======================================================
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1.2f, 4.3f, 1.2f, 1.6f, 1.6f });

        String[] headers = { "CANT.", "DESCRIPCIÓN", "TALLA", "P. UNIT.", "IMPORTE" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontTablaHead));
            cell.setBackgroundColor(COLOR_GRIS_BG);
            cell.setHorizontalAlignment(h.equals("DESCRIPCIÓN") ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
            cell.setPadding(6);
            cell.setBorderColor(COLOR_BORDE);
            table.addCell(cell);
        }

        for (DetallePedido dp : pedido.getDetalles()) {
            table.addCell(celdaFila(String.valueOf(dp.getCantidad()), fontNormal, Element.ALIGN_CENTER));
            table.addCell(celdaFila(dp.getNombreProducto(), fontNormal, Element.ALIGN_LEFT));
            table.addCell(celdaFila(dp.getTalla() != null ? dp.getTalla() : "-", fontNormal, Element.ALIGN_CENTER));
            table.addCell(celdaFila("S/ " + df.format(dp.getPrecioUnitario()), fontNormal, Element.ALIGN_RIGHT));
            table.addCell(celdaFila("S/ " + df.format(dp.getSubtotal()), fontNormal, Element.ALIGN_RIGHT));
        }
        document.add(table);

        // ======================================================
        // 4) TOTALES (caja con borde, alineada a la derecha)
        // ======================================================
        double total = pedido.getMontoTotal();
        double subtotal = total / 1.18;
        double igv = total - subtotal;

        Paragraph espacio2 = new Paragraph(" ");
        espacio2.setSpacingAfter(4);
        document.add(espacio2);

        PdfPTable filaTotales = new PdfPTable(2);
        filaTotales.setWidthPercentage(100);
        filaTotales.setWidths(new float[] { 58f, 42f });

        PdfPCell vacio = new PdfPCell(new Phrase(""));
        vacio.setBorder(0);
        filaTotales.addCell(vacio);

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(100);
        totalsTable.setWidths(new float[] { 55f, 45f });

        addTotalRow(totalsTable, "GRAVADA:", "S/ " + df.format(subtotal), fontNormal, fontNormal, false);
        addTotalRow(totalsTable, "IGV (18%):", "S/ " + df.format(igv), fontNormal, fontNormal, false);
        addTotalRow(totalsTable, "TOTAL:", "S/ " + df.format(total), fontTotalLabel, fontTotalLabel, true);

        PdfPCell contTotals = new PdfPCell(totalsTable);
        contTotals.setBorder(PdfPCell.BOX);
        contTotals.setBorderColor(COLOR_BORDE);
        contTotals.setPadding(4);
        filaTotales.addCell(contTotals);

        document.add(filaTotales);

        // ======================================================
        // 5) IMPORTE EN LETRAS (caja completa con borde)
        // ======================================================
        Paragraph espacio3 = new Paragraph(" ");
        espacio3.setSpacingAfter(4);
        document.add(espacio3);

        PdfPTable cajaLetras = new PdfPTable(1);
        cajaLetras.setWidthPercentage(100);

        Paragraph pLetras = new Paragraph();
        pLetras.add(new Chunk("IMPORTE EN LETRAS: ", fontImporteLet));
        pLetras.add(new Chunk(numeroALetras(total) + " SOLES", fontNormal));
        pLetras.setAlignment(Element.ALIGN_CENTER);

        PdfPCell celdaLetras = new PdfPCell(pLetras);
        celdaLetras.setBorder(PdfPCell.BOX);
        celdaLetras.setBorderColor(COLOR_BORDE);
        celdaLetras.setPadding(8);
        cajaLetras.addCell(celdaLetras);

        document.add(cajaLetras);

        // ======================================================
        // 6) PIE DE PÁGINA
        // ======================================================
        Paragraph espacioFooter = new Paragraph(" ");
        espacioFooter.setSpacingBefore(16);
        document.add(espacioFooter);

        PdfPTable cajaFooter = new PdfPTable(1);
        cajaFooter.setWidthPercentage(100);

        Paragraph pFooter = new Paragraph(
                "Representación impresa de la BOLETA DE VENTA ELECTRÓNICA. Documento generado electrónicamente por PALANT S.A.C.",
                fontFooter);
        PdfPCell celdaFooter = new PdfPCell(pFooter);
        celdaFooter.setBorder(PdfPCell.BOX);
        celdaFooter.setBorderColor(COLOR_BORDE);
        celdaFooter.setPadding(8);
        cajaFooter.addCell(celdaFooter);

        document.add(cajaFooter);

        document.close();
    }

    // ---------------------------------------------------------
    // Helpers de armado visual
    // ---------------------------------------------------------

    private Image cargarLogo() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("static/assets/logo.png")) {
            if (is == null)
                return null;
            byte[] bytes = is.readAllBytes();
            return Image.getInstance(bytes);
        } catch (Exception e) {
            return null; // Si no existe el logo, la boleta se genera igual con el nombre en texto
        }
    }

    private Paragraph filaLabelValor(String etiqueta, String valor, Font fLabel, Font fValor) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta + " : ", fLabel));
        p.add(new Chunk(valor, fValor));
        p.setSpacingAfter(3f);
        return p;
    }

    private PdfPCell celdaFila(String texto, Font font, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(COLOR_BORDE);
        return cell;
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font fLabel, Font fValue, boolean destacado) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, fLabel));
        cell1.setBorder(0);
        cell1.setPadding(4);
        cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (destacado) {
            cell1.setBorder(com.lowagie.text.Rectangle.TOP);
            cell1.setBorderColor(COLOR_BORDE);
        }
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(value, fValue));
        cell2.setBorder(0);
        cell2.setPadding(4);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (destacado) {
            cell2.setBorder(com.lowagie.text.Rectangle.TOP);
            cell2.setBorderColor(COLOR_BORDE);
        }
        table.addCell(cell2);
    }

    // ---------------------------------------------------------
    // Conversión de número a letras (soles) — ej: 45.00 -> "CUARENTA Y CINCO CON
    // 00/100"
    // ---------------------------------------------------------
    private static final String[] UNIDADES = { "", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO",
            "NUEVE" };
    private static final String[] DECENAS = { "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISÉIS",
            "DIECISIETE", "DIECIOCHO", "DIECINUEVE" };
    private static final String[] DIEZ_MULT = { "", "", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA",
            "SETENTA", "OCHENTA", "NOVENTA" };
    private static final String[] CENTENAS = { "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS" };

    private String numeroALetras(double monto) {
        long entero = (long) monto;
        int centavos = (int) Math.round((monto - entero) * 100);
        String letras = (entero == 0) ? "CERO" : convertirEntero(entero);
        return letras + " CON " + String.format("%02d", centavos) + "/100";
    }

    private String convertirEntero(long numero) {
        if (numero == 0)
            return "";
        if (numero < 10)
            return UNIDADES[(int) numero];
        if (numero < 20)
            return DECENAS[(int) numero - 10];
        if (numero < 100) {
            long d = numero / 10, u = numero % 10;
            if (numero < 30)
                return u == 0 ? "VEINTE"
                        : "VEINTI" + UNIDADES[(int) u].toLowerCase().replace("uno", "ún").toUpperCase();
            return DIEZ_MULT[(int) d] + (u > 0 ? " Y " + UNIDADES[(int) u] : "");
        }
        if (numero < 1000) {
            long c = numero / 100, r = numero % 100;
            if (numero == 100)
                return "CIEN";
            return CENTENAS[(int) c] + (r > 0 ? " " + convertirEntero(r) : "");
        }
        if (numero < 1000000) {
            long miles = numero / 1000, r = numero % 1000;
            String prefijoMiles = (miles == 1) ? "MIL" : convertirEntero(miles) + " MIL";
            return prefijoMiles + (r > 0 ? " " + convertirEntero(r) : "");
        }
        return String.valueOf(numero);
    }
}