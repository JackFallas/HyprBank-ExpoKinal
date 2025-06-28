package com.hyprbank.online.bancavirtual.service;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.model.AccesoUsuario;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.dto.ReporteAccesoDTO;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.repository.AccesoUsuarioRepository; // Ajuste de nombre de Repositorio

// Importaciones de JasperReports
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

// Importaciones de Java Utilities e IO
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Implementacion de la interfaz ReporteServicio.
 *
 * Esta clase es responsable de la logica para generar reportes en diferentes formatos,
 * utilizando la libreria JasperReports. Actualmente, genera un reporte PDF de accesos de usuarios.
 */
@Service
public class ReporteServicioImpl implements ReporteServicio {

    private final AccesoUsuarioRepository accesoUsuarioRepository;

    /*
     * Constructor para inyeccion de dependencias.
     * Spring inyectara la instancia de AccesoUsuarioRepository.
     */
    @Autowired
    public ReporteServicioImpl(AccesoUsuarioRepository accesoUsuarioRepository) {
        this.accesoUsuarioRepository = accesoUsuarioRepository;
    }

    /**
     * Genera un reporte PDF con el historial de accesos de usuarios.
     *
     * Este metodo compila el archivo JRXML del reporte, obtiene los datos de accesos de la base de datos,
     * los mapea a un DTO especifico para el reporte, llena el reporte con estos datos y lo exporta
     * a un arreglo de bytes en formato PDF.
     *
     * @return Un arreglo de bytes que representa el documento PDF generado.
     * @throws FileNotFoundException Si el archivo .jrxml del reporte no se encuentra en la ruta especificada.
     * @throws JRException Si ocurre un error durante la compilacion, llenado o exportacion del reporte con JasperReports.
     * @throws IOException Si ocurre un error de entrada/salida al manejar el stream del PDF.
     */
    @Override
    public byte[] generarReporteAccesosUsuariosPdf() throws FileNotFoundException, JRException, IOException {
        // 1. Cargar y compilar el archivo JRXML del reporte.
        // Se carga el archivo de definicion del reporte desde la ruta de recursos del classpath.
        File file = ResourceUtils.getFile("classpath:reports/reporte_accesos_usuarios.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

        // 2. Obtener los datos necesarios para el reporte desde el repositorio.
        List<AccesoUsuario> accesos = accesoUsuarioRepository.findAll();

        // 3. Mapear los datos de las entidades AccesoUsuario a objetos DTO especificos para el reporte.
        // Esto desacopla la logica del reporte de la estructura de la entidad JPA.
        List<ReporteAccesoDTO> data = new ArrayList<>();
        for (AccesoUsuario acceso : accesos) {
            ReporteAccesoDTO dto = new ReporteAccesoDTO();
            if (acceso.getUsuario() != null) {
                // Si hay un usuario asociado al acceso, se obtienen sus datos.
                dto.setNombreUsuario(acceso.getUsuario().getNombre());
                dto.setApellidoUsuario(acceso.getUsuario().getApellido());
                dto.setEmailUsuario(acceso.getUsuario().getEmail());
            } else {
                // Para casos donde el acceso fallo y no hay un usuario asociado o es null.
                dto.setNombreUsuario("N/A");
                dto.setApellidoUsuario("N/A");
                dto.setEmailUsuario("N/A");
            }
            dto.setFechaHoraAcceso(acceso.getFechaHoraAcceso());
            dto.setTipoAcceso(acceso.getTipoAcceso());
            dto.setIpAddress(acceso.getIpAddress());
            data.add(dto);
        }

        // Crear la fuente de datos de JasperReports a partir de la lista de DTOs.
        // JRBeanCollectionDataSource permite a JasperReports iterar sobre una coleccion de Java Beans.
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

        // 4. Definir los parametros del reporte (actualmente vacios, se pueden agregar mas si es necesario).
        Map<String, Object> parameters = new HashMap<>();
        // Ejemplo: parameters.put("tituloReporte", "Reporte de Accesos de Usuarios");

        // 5. Llenar el reporte: se combina la plantilla compilada, los parametros y la fuente de datos.
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 6. Exportar el reporte lleno a un arreglo de bytes en formato PDF.
        // Se utiliza un ByteArrayOutputStream para escribir el PDF directamente en memoria.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        return outputStream.toByteArray();
    }
}