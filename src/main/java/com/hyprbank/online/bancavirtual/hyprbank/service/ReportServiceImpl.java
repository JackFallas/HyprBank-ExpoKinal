package com.hyprbank.online.bancavirtual.hyprbank.service;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.UserAccess;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.AccessReportDTO;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserAccessRepository;

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
 * Implementacion de la interfaz {@link ReportService}.
 *
 * Esta clase se encarga de la logica de negocio para la generacion de reportes
 * utilizando la libreria JasperReports.
 *
 * @Service indica que esta clase es un componente de servicio de Spring.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final UserAccessRepository userAccessRepository;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara la instancia de UserAccessRepository.
     */
    @Autowired
    public ReportServiceImpl(UserAccessRepository userAccessRepository) {
        this.userAccessRepository = userAccessRepository;
    }

    /**
     * Genera un reporte de accesos de usuarios en formato PDF.
     *
     * Este metodo:
     * 1. Carga el archivo de definicion del reporte (.jrxml) desde los recursos.
     * 2. Compila el archivo JRXML a un objeto JasperReport.
     * 3. Obtiene todos los registros de acceso de usuarios de la base de datos.
     * 4. Mapea las entidades {@link UserAccess} a {@link AccessReportDTO} para el reporte.
     * 5. Crea una fuente de datos para JasperReports a partir de la coleccion de DTOs.
     * 6. Llena el reporte con los datos.
     * 7. Exporta el reporte lleno a un arreglo de bytes en formato PDF.
     *
     * @return Un arreglo de bytes que representa el documento PDF generado.
     * @throws FileNotFoundException Si el archivo JRXML no se encuentra.
     * @throws JRException Si ocurre un error durante la compilacion, llenado o exportacion del reporte.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public byte[] generateUserAccessReportPdf() throws FileNotFoundException, JRException, IOException {
        // 1. Cargar el archivo JRXML desde los recursos.
        // Asegurate de que el archivo 'user_access_report.jrxml' este en src/main/resources/reports/
        File file = ResourceUtils.getFile("classpath:reports/user_access_report.jrxml");

        // 2. Compilar el reporte JRXML.
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

        // 3. Obtener los datos de accesos de usuarios.
        List<UserAccess> accesses = userAccessRepository.findAll();

        // 4. Mapear las entidades AccesoUsuario a AccessReportDTOs.
        List<AccessReportDTO> data = new ArrayList<>();
        for (UserAccess access : accesses) {
            AccessReportDTO dto = new AccessReportDTO();
            // Asegurarse de manejar el caso donde el usuario podria ser null (ej. login fallido de usuario inexistente)
            if (access.getUser() != null) {
                dto.setUserName(access.getUser().getFirstName());
                dto.setUserLastName(access.getUser().getLastName());
                dto.setUserEmail(access.getUser().getEmail());
            } else {
                // Valores por defecto si el usuario asociado al acceso es nulo
                dto.setUserName("N/A");
                dto.setUserLastName("N/A");
                dto.setUserEmail("N/A");
            }
            dto.setAccessDateTime(access.getAccessDateTime());
            dto.setAccessType(access.getAccessType());
            dto.setIpAddress(access.getIpAddress());
            data.add(dto);
        }

        // Crear la fuente de datos de JasperReports a partir de la lista de DTOs.
        // JRBeanCollectionDataSource permite a JasperReports iterar sobre una coleccion de Java Beans.
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

        // 5. Definir los parametros del reporte (actualmente vacios, se pueden agregar mas si es necesario).
        Map<String, Object> parameters = new HashMap<>();
        // Ejemplo: parameters.put("reportTitle", "Reporte de Accesos de Usuarios");

        // 6. Llenar el reporte: se combina la plantilla compilada, los parametros y la fuente de datos.
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 7. Exportar el reporte lleno a un arreglo de bytes en formato PDF.
        // Se utiliza un ByteArrayOutputStream para escribir el PDF directamente en memoria.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        return outputStream.toByteArray();
    }
}