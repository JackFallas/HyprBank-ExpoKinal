package com.hyprbank.online.bancavirtual.hyprbank.service;
// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.UserAccess;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.model.Account; // Necesaria para el mapeo de movimientos
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement.MovementType; // Necesaria para el mapeo de movimientos

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.AccessReportDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.ClientReportDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminMovementDTO;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserAccessRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository; // ¡Añadido!
import com.hyprbank.online.bancavirtual.hyprbank.repository.MovementRepository; // ¡Añadido!


// Importaciones de JasperReports
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager; // Puede que no sea estrictamente necesario con JRPdfExporter
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.engine.export.JRPdfExporter;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils; // Aunque se recomienda getResourceAsStream, mantenemos si lo usas

// Importaciones de Java Utilities e IO
import java.io.ByteArrayOutputStream;
import java.io.File; // Para ResourceUtils.getFile
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Para usar Streams API

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
    private final UserRepository userRepository; // Correcto: inyectamos UserRepository
    private final MovementRepository movementRepository; // Correcto: inyectamos MovementRepository


    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de UserAccessRepository, UserRepository y MovementRepository.
     */
    @Autowired
    public ReportServiceImpl(UserAccessRepository userAccessRepository,
                             UserRepository userRepository,
                             MovementRepository movementRepository) {
        this.userAccessRepository = userAccessRepository;
        this.userRepository = userRepository; // Inicializamos
        this.movementRepository = movementRepository; // Inicializamos
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
        // Usamos getClass().getResourceAsStream() para mayor robustez en entornos empaquetados (JAR)
        InputStream reportStream = getClass().getResourceAsStream("/reports/reporte_accesos_usuarios.jrxml");
        if (reportStream == null) {
            throw new FileNotFoundException("Reporte JRXML de acceso de usuarios no encontrado en classpath: /reports/user_access_report.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        List<UserAccess> accesses = userAccessRepository.findAll();

        List<AccessReportDTO> data = new ArrayList<>();
        for (UserAccess access : accesses) {
            AccessReportDTO dto = new AccessReportDTO();
            if (access.getUser() != null) {
                dto.setUserName(access.getUser().getFirstName());
                dto.setUserLastName(access.getUser().getLastName());
                dto.setUserEmail(access.getUser().getEmail());
            } else {
                dto.setUserName("N/A");
                dto.setUserLastName("N/A");
                dto.setUserEmail("N/A");
            }
            dto.setAccessDateTime(access.getAccessDateTime());
            dto.setAccessType(access.getAccessType());
            dto.setIpAddress(access.getIpAddress());
            data.add(dto);
        }

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

        Map<String, Object> parameters = new HashMap<>();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();

        return outputStream.toByteArray();
    }

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
    @Override
    public byte[] generateClientReportPdf() throws FileNotFoundException, JRException, IOException {
        // 1. Obtener los datos directamente del userRepository y mapearlos a DTOs
        List<User> users = userRepository.findAll(); // ¡Ahora podemos usar userRepository!
        List<ClientReportDTO> clients = users.stream().map(user -> {
            ClientReportDTO dto = new ClientReportDTO();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setDpi(user.getDpi());
            dto.setNit(user.getNit());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setEnabled(user.isEnabled());
            return dto;
        }).collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(clients);

        // 2. Cargar y compilar el archivo JRXML del reporte de clientes
        InputStream reportStream = getClass().getResourceAsStream("/reports/clients_report.jrxml");
        if (reportStream == null) {
            throw new FileNotFoundException("Reporte JRXML de clientes no encontrado en classpath: /reports/client_report.jrxml");
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // 3. Parámetros del reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Reporte de Clientes");

        // 4. Llenar el reporte con los datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 5. Exportar el reporte a PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        exporter.exportReport();

        return baos.toByteArray();
    }

    /**
     * Genera un reporte de todos los movimientos (transacciones) en formato PDF.
     * Similar al reporte de clientes, pero para la información de los movimientos.
     *
     * @return Un arreglo de bytes que representa el documento PDF generado.
     * @throws FileNotFoundException Si el archivo de definición del reporte (JRXML) no se encuentra.
     * @throws JRException Si ocurre un error durante la generación del reporte con JasperReports.
     * @throws IOException Si ocurre un error de entrada/salida durante la escritura del PDF.
     */
    @Override
    public byte[] generateMovementReportPdf() throws FileNotFoundException, JRException, IOException {
        // 1. Obtener los datos directamente del movementRepository y mapearlos a DTOs
       
        List<Movement> rawMovements = movementRepository.findAll(); 
        List<AdminMovementDTO> movements = rawMovements.stream().map(movement -> {
            AdminMovementDTO dto = new AdminMovementDTO();
            dto.setId(movement.getId());
            dto.setDate(movement.getDate());
            dto.setDescription(movement.getDescription());
            dto.setType(movement.getType().name()); // Convertir el enum a String
            dto.setAmount(movement.getAmount());

            // Para obtener el número de cuenta y el nombre del usuario
            // Asegúrate de que tus entidades Movement y Account tienen las relaciones JPA correctas
            if (movement.getAccount() != null) {
                Account account = movement.getAccount();
                dto.setAccountNumber(account.getAccountNumber());

                if (account.getUser() != null) {
                    User user = account.getUser();
                    dto.setUserName(user.getFirstName() + " " + user.getLastName());
                } else {
                    dto.setUserName("Usuario Desconocido");
                }
            } else {
                dto.setAccountNumber("N/A");
                dto.setUserName("Cuenta Desconocida");
            }
            return dto; 
        }).collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(movements);

        // 2. Cargar y compilar el archivo JRXML del reporte de movimientos
        InputStream reportStream = getClass().getResourceAsStream("/reports/transactions_report.jrxml");
        if (reportStream == null) {
            throw new FileNotFoundException("Reporte JRXML de movimientos no encontrado en classpath: /reports/transactions_report.jrxml");
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // 3. Parámetros del reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Reporte de Movimientos Bancarios");

        // 4. Llenar el reporte con los datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 5. Exportar el reporte a PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        exporter.exportReport();

        return baos.toByteArray();
    }
}