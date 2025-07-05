package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.ReportService;

// Importaciones de JasperReports
import net.sf.jasperreports.engine.JRException;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus; // Importar HttpStatus para ResponseEntity.status()
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importaciones de Java IO
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * Controlador REST para la generación y descarga de reportes.
 *
 * Proporciona un endpoint para que los usuarios puedan generar y descargar
 * reportes específicos en formato PDF, como el historial de accesos de usuarios,
 * clientes y movimientos.
 *
 * La anotación @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los métodos se serializarán directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/reports") define la ruta base para todos los endpoints de este controlador.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportRestController { // Cambiado de ReportRestController a ReportController en versiones anteriores

    private final ReportService reportService;

    /*
     * Constructor para la inyección de dependencias.
     * Spring inyectará la instancia de ReportService.
     */
    @Autowired
    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Endpoint para generar y descargar el reporte de accesos de usuarios en formato PDF.
     *
     * Este método invoca al servicio para generar el reporte y configura las cabeceras
     * HTTP adecuadas para que el navegador lo identifique como un archivo PDF para descargar.
     *
     * @return ResponseEntity<byte[]> que contiene los bytes del PDF generado en el cuerpo de la respuesta,
     * junto con las cabeceras para la descarga.
     * Retorna un error 500 Internal Server Error si ocurre alguna excepcion durante la generacion del reporte.
     */
    @GetMapping("/user-access/pdf")
    public ResponseEntity<byte[]> generateUserAccessReportPdf() {
        try {
            // Llama al servicio para generar el reporte como un arreglo de bytes.
            byte[] pdfBytes = reportService.generateUserAccessReportPdf();

            // Configura las cabeceras de la respuesta HTTP para la descarga del PDF.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "user_access_report.pdf"); // inline para mostrar en el navegador
            headers.setContentLength(pdfBytes.length);

            // Retorna la respuesta con el PDF en el cuerpo, las cabeceras configuradas y el estado HTTP 200 OK.
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (FileNotFoundException e) {
            // Maneja la excepcion si el archivo .jrxml del reporte no se encuentra.
            // Imprime la traza para depuración en el servidor
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("Error: Archivo de reporte de acceso de usuarios no encontrado. " + e.getMessage()).getBytes());
        } catch (JRException e) {
            // Maneja la excepcion si hay un error especifico de JasperReports (compilacion, llenado, etc.).
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error al generar el reporte de acceso de usuarios con JasperReports. " + e.getMessage()).getBytes());
        } catch (IOException e) {
            // Maneja la excepcion si ocurre un error de entrada/salida al escribir el PDF.
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error de I/O al generar el PDF de acceso de usuarios. " + e.getMessage()).getBytes());
        } catch (Exception e) {
            // Captura cualquier otra excepcion inesperada y devuelve un mensaje de error generico.
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Ocurrió un error inesperado al generar el reporte de acceso de usuarios. " + e.getMessage()).getBytes());
        }
    }

    /**
     * Endpoint para generar el reporte de clientes en formato PDF.
     * Accessible a través de GET /api/reports/clients/pdf.
     *
     * @return Una ResponseEntity que contiene el arreglo de bytes del PDF
     * y las cabeceras HTTP adecuadas para la descarga del archivo.
     */
    @GetMapping("/clients/pdf")
    public ResponseEntity<byte[]> getClientReportPdf() {
        try {
            // Llama al servicio para generar el PDF de clientes
            byte[] pdfBytes = reportService.generateClientReportPdf();

            // Configura las cabeceras HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "reporte_clientes.pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("Error: Plantilla de reporte de clientes no encontrada. " + e.getMessage()).getBytes());
        } catch (JRException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error al generar el reporte de clientes con JasperReports. " + e.getMessage()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error de I/O al generar el PDF de clientes. " + e.getMessage()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Ocurrió un error inesperado al generar el reporte de clientes. " + e.getMessage()).getBytes());
        }
    }

    /**
     * Endpoint para generar el reporte de movimientos en formato PDF.
     * Accessible a través de GET /api/reports/movements/pdf.
     *
     * @return Una ResponseEntity que contiene el arreglo de bytes del PDF
     * y las cabeceras HTTP adecuadas para la descarga del archivo.
     */
    @GetMapping("/movements/pdf")
    public ResponseEntity<byte[]> getMovementReportPdf() {
        try {
            // Llama al servicio para generar el PDF de movimientos
            byte[] pdfBytes = reportService.generateMovementReportPdf();

            // Configura las cabeceras HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "reporte_movimientos.pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("Error: Plantilla de reporte de movimientos no encontrada. " + e.getMessage()).getBytes());
        } catch (JRException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error al generar el reporte de movimientos con JasperReports. " + e.getMessage()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error de I/O al generar el PDF de movimientos. " + e.getMessage()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Ocurrió un error inesperado al generar el reporte de movimientos. " + e.getMessage()).getBytes());
        }
    }
}
