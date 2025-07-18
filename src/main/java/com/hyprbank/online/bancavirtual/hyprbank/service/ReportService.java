package com.hyprbank.online.bancavirtual.hyprbank.service;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.sf.jasperreports.engine.JRException;

/*
 * Interfaz de Servicio para la generacion de reportes.
 *
 * Define los metodos que seran implementados por la clase encargada de crear y gestionar reportes,
 * como los de JasperReports.
 */

public interface ReportService {

    /**
     * Genera un reporte de accesos de usuarios en formato PDF.
     * Este metodo es responsable de compilar el archivo JRXML, llenar el reporte con datos
     * y exportarlo a un arreglo de bytes en formato PDF.
     *
     * @return Un arreglo de bytes que representa el documento PDF generado.
     * @throws FileNotFoundException Si el archivo de definicion del reporte (JRXML) no se encuentra.
     * @throws JRException Si ocurre un error durante la generacion del reporte con JasperReports.
     * @throws IOException Si ocurre un error de entrada/salida durante la escritura del PDF.
     */
    byte[] generateUserAccessReportPdf() throws FileNotFoundException, JRException, IOException;

    /**
     * Genera un reporte de clientes en formato PDF.
     * Este método es responsable de compilar el archivo JRXML, llenar el reporte con datos
     * y exportarlo a un arreglo de bytes en formato PDF.
     *
     * @return Un arreglo de bytes que representa el documento PDF generado.
     * @throws FileNotFoundException Si el archivo de definición del reporte (JRXML) no se encuentra.
     * @throws JRException Si ocurre un error durante la generación del reporte con JasperReports.
     * @throws IOException Si ocurre un error de entrada/salida durante la escritura del PDF.
     */
    byte[] generateClientReportPdf() throws FileNotFoundException, JRException, IOException;

    /**
     * Genera un reporte de todos los movimientos (transacciones) en formato PDF.
     * Similar al reporte de clientes, pero para la información de los movimientos.
     *
     * @return Un arreglo de bytes que representa el documento PDF generado.
     * @throws FileNotFoundException Si el archivo de definición del reporte (JRXML) no se encuentra.
     * @throws JRException Si ocurre un error durante la generación del reporte con JasperReports.
     * @throws IOException Si ocurre un error de entrada/salida durante la escritura del PDF.
     */
    byte[] generateMovementReportPdf() throws FileNotFoundException, JRException, IOException;
}
