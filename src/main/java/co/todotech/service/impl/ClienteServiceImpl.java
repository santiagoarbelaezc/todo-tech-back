package co.todotech.service.impl;

import co.todotech.mapper.ClienteMapper;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.entities.Cliente;
import co.todotech.model.enums.TipoCliente;
import co.todotech.repository.ClienteRepository;
import co.todotech.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteMapper clienteMapper;
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public ClienteDto crearCliente(ClienteDto dto) throws Exception {
        log.info("MONITORING_CLIENT_CREATE - action: CREATE_START, cedula: {}, nombre: {}",
                dto.cedula(), dto.nombre());

        try {
            // Validar unicidad de cédula
            if (clienteRepository.existsByCedula(dto.cedula())) {
                log.warn("MONITORING_CLIENT_CREATE - action: CREATE_FAILED, cedula: {}, reason: DUPLICATE_CEDULA",
                        dto.cedula());
                throw new Exception("Ya existe un cliente con la cédula: " + dto.cedula());
            }

            // Validar unicidad de correo si se proporciona
            if (dto.correo() != null && !dto.correo().isBlank() &&
                    clienteRepository.existsByCorreo(dto.correo())) {
                log.warn("MONITORING_CLIENT_CREATE - action: CREATE_FAILED, correo: {}, reason: DUPLICATE_EMAIL",
                        dto.correo());
                throw new Exception("Ya existe un cliente con el correo: " + dto.correo());
            }

            Cliente cliente = clienteMapper.toEntity(dto);
            clienteRepository.save(cliente);

            // ✅ LOG ESTRUCTURADO PARA MONITOREO
            log.info("MONITORING_CLIENT_CREATE - action: CREATE_SUCCESS, clienteId: {}, cedula: {}, nombre: {}, tipo: {}, fechaRegistro: {}",
                    cliente.getId(), cliente.getCedula(), cliente.getNombre(),
                    cliente.getTipoCliente(), cliente.getFechaRegistro());

            return clienteMapper.toDto(cliente);

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_CREATE - action: CREATE_ERROR, cedula: {}, error: {}",
                    dto.cedula(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public ClienteDto actualizarCliente(Long id, ClienteDto dto) throws Exception {
        log.info("MONITORING_CLIENT_UPDATE - action: UPDATE_START, clienteId: {}, cedula: {}", id, dto.cedula());

        try {
            Cliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("MONITORING_CLIENT_UPDATE - action: UPDATE_FAILED, clienteId: {}, reason: NOT_FOUND", id);
                        return new Exception("Cliente no encontrado con ID: " + id);
                    });

            // Validar unicidad de cédula contra otros registros
            if (!cliente.getCedula().equals(dto.cedula()) &&
                    clienteRepository.existsByCedulaAndIdNot(dto.cedula(), id)) {
                log.warn("MONITORING_CLIENT_UPDATE - action: UPDATE_FAILED, clienteId: {}, cedula: {}, reason: DUPLICATE_CEDULA",
                        id, dto.cedula());
                throw new Exception("Ya existe otro cliente con la cédula: " + dto.cedula());
            }

            // Validar unicidad de correo contra otros registros
            if (dto.correo() != null && !dto.correo().isBlank() &&
                    !dto.correo().equals(cliente.getCorreo()) &&
                    clienteRepository.existsByCorreoAndIdNot(dto.correo(), id)) {
                log.warn("MONITORING_CLIENT_UPDATE - action: UPDATE_FAILED, clienteId: {}, correo: {}, reason: DUPLICATE_EMAIL",
                        id, dto.correo());
                throw new Exception("Ya existe otro cliente con el correo: " + dto.correo());
            }

            // Guardar datos antiguos para log
            String nombreAnterior = cliente.getNombre();
            String cedulaAnterior = cliente.getCedula();
            TipoCliente tipoAnterior = cliente.getTipoCliente();

            clienteMapper.updateClienteFromDto(dto, cliente);
            clienteRepository.save(cliente);

            // ✅ LOG ESTRUCTURADO PARA MONITOREO
            log.info("MONITORING_CLIENT_UPDATE - action: UPDATE_SUCCESS, clienteId: {}, " +
                            "changes: {nombre: {}→{}, cedula: {}→{}, tipo: {}→{}}",
                    id, nombreAnterior, cliente.getNombre(), cedulaAnterior,
                    cliente.getCedula(), tipoAnterior, cliente.getTipoCliente());

            return clienteMapper.toDto(cliente);

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_UPDATE - action: UPDATE_ERROR, clienteId: {}, error: {}",
                    id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void eliminarCliente(Long id) throws Exception {
        log.info("MONITORING_CLIENT_DELETE - action: DELETE_START, clienteId: {}", id);

        try {
            Cliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("MONITORING_CLIENT_DELETE - action: DELETE_FAILED, clienteId: {}, reason: NOT_FOUND", id);
                        return new Exception("Cliente no encontrado con ID: " + id);
                    });

            // Aquí podrías agregar validaciones adicionales (ej: si tiene órdenes activas)
            // if (!cliente.getOrdenes().isEmpty()) {
            //     log.warn("MONITORING_CLIENT_DELETE - action: DELETE_FAILED, clienteId: {}, reason: HAS_ACTIVE_ORDERS", id);
            //     throw new Exception("No se puede eliminar cliente con órdenes activas");
            // }

            clienteRepository.delete(cliente);

            // ✅ LOG ESTRUCTURADO PARA MONITOREO
            log.info("MONITORING_CLIENT_DELETE - action: DELETE_SUCCESS, clienteId: {}, cedula: {}, nombre: {}",
                    id, cliente.getCedula(), cliente.getNombre());

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_DELETE - action: DELETE_ERROR, clienteId: {}, error: {}",
                    id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDto obtenerClientePorId(Long id) throws Exception {
        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_ID, clienteId: {}", id);

        try {
            Cliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("MONITORING_CLIENT_QUERY - action: GET_BY_ID_FAILED, clienteId: {}, reason: NOT_FOUND", id);
                        return new Exception("Cliente no encontrado con ID: " + id);
                    });

            log.info("MONITORING_CLIENT_QUERY - action: GET_BY_ID_SUCCESS, clienteId: {}, cedula: {}, nombre: {}",
                    id, cliente.getCedula(), cliente.getNombre());

            return clienteMapper.toDto(cliente);

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_QUERY - action: GET_BY_ID_ERROR, clienteId: {}, error: {}",
                    id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDto obtenerClientePorCedula(String cedula) throws Exception {
        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_CEDULA, cedula: {}", cedula);

        try {
            Cliente cliente = clienteRepository.findByCedula(cedula)
                    .orElseThrow(() -> {
                        log.warn("MONITORING_CLIENT_QUERY - action: GET_BY_CEDULA_FAILED, cedula: {}, reason: NOT_FOUND", cedula);
                        return new Exception("Cliente no encontrado con cédula: " + cedula);
                    });

            log.info("MONITORING_CLIENT_QUERY - action: GET_BY_CEDULA_SUCCESS, cedula: {}, clienteId: {}, nombre: {}",
                    cedula, cliente.getId(), cliente.getNombre());

            return clienteMapper.toDto(cliente);

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_QUERY - action: GET_BY_CEDULA_ERROR, cedula: {}, error: {}",
                    cedula, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDto obtenerClientePorCorreo(String correo) throws Exception {
        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_EMAIL, correo: {}", correo);

        try {
            Cliente cliente = clienteRepository.findByCorreo(correo)
                    .orElseThrow(() -> {
                        log.warn("MONITORING_CLIENT_QUERY - action: GET_BY_EMAIL_FAILED, correo: {}, reason: NOT_FOUND", correo);
                        return new Exception("Cliente no encontrado con correo: " + correo);
                    });

            log.info("MONITORING_CLIENT_QUERY - action: GET_BY_EMAIL_SUCCESS, correo: {}, clienteId: {}, nombre: {}",
                    correo, cliente.getId(), cliente.getNombre());

            return clienteMapper.toDto(cliente);

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_QUERY - action: GET_BY_EMAIL_ERROR, correo: {}, error: {}",
                    correo, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDto> obtenerClientesPorTipo(TipoCliente tipoCliente) {
        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_TYPE, tipo: {}", tipoCliente);

        List<ClienteDto> clientes = clienteRepository.findByTipoCliente(tipoCliente).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());

        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_TYPE_SUCCESS, tipo: {}, count: {}",
                tipoCliente, clientes.size());

        return clientes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDto> obtenerClientesPorNombre(String nombre) {
        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_NAME, nombre: {}", nombre);

        List<ClienteDto> clientes = clienteRepository.findByNombreContaining(nombre).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());

        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_NAME_SUCCESS, nombre: {}, count: {}",
                nombre, clientes.size());

        return clientes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDto> obtenerClientesRegistradosDespuesDe(LocalDateTime fecha) {
        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_REGISTRATION_DATE_AFTER, fecha: {}", fecha);

        List<ClienteDto> clientes = clienteRepository.findByFechaRegistroAfter(fecha).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());

        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_REGISTRATION_DATE_AFTER_SUCCESS, fecha: {}, count: {}",
                fecha, clientes.size());

        return clientes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDto> obtenerClientesRegistradosEntre(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_REGISTRATION_DATE_RANGE, start: {}, end: {}",
                fechaInicio, fechaFin);

        List<ClienteDto> clientes = clienteRepository.findByFechaRegistroBetween(fechaInicio, fechaFin).stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());

        log.info("MONITORING_CLIENT_QUERY - action: GET_BY_REGISTRATION_DATE_RANGE_SUCCESS, start: {}, end: {}, count: {}",
                fechaInicio, fechaFin, clientes.size());

        return clientes;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarClientesPorTipo(TipoCliente tipoCliente) {
        log.info("MONITORING_CLIENT_QUERY - action: COUNT_BY_TYPE, tipo: {}", tipoCliente);

        long count = clienteRepository.countByTipoCliente(tipoCliente);

        log.info("MONITORING_CLIENT_QUERY - action: COUNT_BY_TYPE_SUCCESS, tipo: {}, count: {}",
                tipoCliente, count);

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDto> obtenerTodosLosClientes() {
        log.info("MONITORING_CLIENT_QUERY - action: GET_ALL");

        List<ClienteDto> clientes = clienteRepository.findAllOrderedByFechaRegistro().stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());

        log.info("MONITORING_CLIENT_QUERY - action: GET_ALL_SUCCESS, total: {}", clientes.size());

        return clientes;
    }

    // ✅ MÉTODOS ESPECÍFICOS PARA MONITOREO

    /**
     * ✅ MÉTODO PARA MONITOREO: Estadísticas generales de clientes
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getClientesSummaryForMonitoring() {
        log.info("MONITORING_CLIENT_SUMMARY - action: GENERATE_REPORT");

        try {
            List<Cliente> clientes = clienteRepository.findAll();

            // Conteo por tipo de cliente
            Map<TipoCliente, Long> conteoPorTipo = clientes.stream()
                    .collect(Collectors.groupingBy(Cliente::getTipoCliente, Collectors.counting()));

            // Clientes registrados hoy
            long clientesHoy = clientes.stream()
                    .filter(cliente -> cliente.getFechaRegistro().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                    .count();

            // Cliente más reciente
            Cliente clienteReciente = clientes.stream()
                    .max((c1, c2) -> c1.getFechaRegistro().compareTo(c2.getFechaRegistro()))
                    .orElse(null);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalClientes", clientes.size());
            summary.put("conteoPorTipo", conteoPorTipo);
            summary.put("clientesRegistradosHoy", clientesHoy);
            summary.put("clienteMasReciente", clienteReciente != null ?
                    Map.of("id", clienteReciente.getId(),
                            "nombre", clienteReciente.getNombre(),
                            "fechaRegistro", clienteReciente.getFechaRegistro().toString()) : null);
            summary.put("timestamp", LocalDateTime.now().toString());

            // ✅ LOG ESTRUCTURADO PARA DASHBOARD
            log.info("MONITORING_CLIENT_SUMMARY - totalClientes: {}, tipos: {}, nuevosHoy: {}, ultimoRegistro: {}",
                    clientes.size(), conteoPorTipo, clientesHoy,
                    clienteReciente != null ? clienteReciente.getFechaRegistro() : "N/A");

            return summary;

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_SUMMARY - action: GENERATE_REPORT_ERROR, error: {}", e.getMessage());
            return Map.of("error", "Error generando reporte de clientes", "timestamp", LocalDateTime.now().toString());
        }
    }

    /**
     * ✅ MÉTODO PARA MONITOREO: Información detallada de un cliente
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getClienteStatusForMonitoring(Long clienteId) {
        log.info("MONITORING_CLIENT_STATUS - action: QUERY, clienteId: {}", clienteId);

        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            Map<String, Object> status = new HashMap<>();
            status.put("clienteId", cliente.getId());
            status.put("cedula", cliente.getCedula());
            status.put("nombre", cliente.getNombre());
            status.put("correo", cliente.getCorreo());
            status.put("telefono", cliente.getTelefono());
            status.put("tipoCliente", cliente.getTipoCliente().toString());
            status.put("fechaRegistro", cliente.getFechaRegistro().toString());
            status.put("direccion", cliente.getDireccion());


            // ✅ LOG ESTRUCTURADO PARA CLOUDWATCH
            log.info("MONITORING_CLIENT_STATUS - clienteId: {}, cedula: {}, nombre: {}, tipo: {}, activo: {}",
                    clienteId, cliente.getCedula(), cliente.getNombre(),
                    cliente.getTipoCliente());

            return status;

        } catch (Exception e) {
            log.error("MONITORING_CLIENT_STATUS_ERROR - clienteId: {}, error: {}", clienteId, e.getMessage());
            return Map.of("error", "Cliente no encontrado", "clienteId", clienteId);
        }
    }

    /**
     * ✅ MÉTODO PARA MONITOREO: Clientes por tipo específico
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getClientesPorTipoForMonitoring(TipoCliente tipo) {
        log.info("MONITORING_CLIENT_BY_TYPE - action: QUERY, tipo: {}", tipo);

        return clienteRepository.findByTipoCliente(tipo)
                .stream()
                .map(cliente -> {
                    Map<String, Object> clientInfo = new HashMap<>();
                    clientInfo.put("clienteId", cliente.getId());
                    clientInfo.put("cedula", cliente.getCedula());
                    clientInfo.put("nombre", cliente.getNombre());
                    clientInfo.put("correo", cliente.getCorreo());
                    clientInfo.put("telefono", cliente.getTelefono());
                    clientInfo.put("fechaRegistro", cliente.getFechaRegistro().toString());

                    return clientInfo;
                })
                .collect(Collectors.toList());
    }
}