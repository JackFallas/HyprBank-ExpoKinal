package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.ReportService;

// Importaciones de JasperReports
import net.sf.jasperreports.engine.JRException;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importaciones de Java IO
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * Controlador REST para la generacion y descarga de reportes.
 *
 * Proporciona un endpoint para que los usuarios puedan generar y descargar
 * reportes especificos en formato PDF, como el historial de accesos de usuarios.
 *
 * La anotacion @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los metodos se serializaran directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/reports") define la ruta base para todos los endpoints de este controlador.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportRestController {

    private final ReportService reportService;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara la instancia de ReportService.
     */
    @Autowired
    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Endpoint para generar y descargar el reporte de accesos de usuarios en formato PDF.
     *
     * Este metodo invoca al servicio para generar el reporte y configura las cabeceras
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
            headers.setContentDispositionFormData("filename", "user_access_report.pdf");
            headers.setContentLength(pdfBytes.length);

            // Retorna la respuesta con el PDF en el cuerpo, las cabeceras configuradas y el estado HTTP 200 OK.
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (FileNotFoundException e) {
            // Maneja la excepcion si el archivo .jrxml del reporte no se encuentra.
            return ResponseEntity.internalServerError().body(("Error: Archivo de reporte no encontrado. " + e.getMessage()).getBytes());
        } catch (JRException e) {
            // Maneja la excepcion si hay un error especifico de JasperReports (compilacion, llenado, etc.).
            return ResponseEntity.internalServerError().body(("Error al generar el reporte Jasper. " + e.getMessage()).getBytes());
        } catch (IOException e) {
            // Maneja la excepcion si ocurre un error de entrada/salida al escribir el PDF.
            return ResponseEntity.internalServerError().body(("Error de I/O al generar el PDF. " + e.getMessage()).getBytes());
        } catch (Exception e) {
            // Captura cualquier otra excepcion inesperada y devuelve un mensaje de error generico.
            return ResponseEntity.internalServerError().body(("Ocurrio un error inesperado al generar el reporte. " + e.getMessage()).getBytes());
        }
    }
}