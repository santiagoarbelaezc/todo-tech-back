package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.enums.TipoCliente;
import co.todotech.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<ClienteDto>> crearCliente(@Valid @RequestBody ClienteDto dto) {
        try {
            log.info("👤 CLIENT_CREATE - Iniciando creación de cliente: {}", dto.nombre());

            ClienteDto clienteCreado = clienteService.crearCliente(dto);

            log.info("✅ CLIENT_CREATE_SUCCESS - Cliente creado exitosamente: id={}, cedula={}",
                    clienteCreado.id(), clienteCreado.cedula());

            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente creado exitosamente", clienteCreado));
        } catch (Exception e) {
            log.error("❌ CLIENT_CREATE_ERROR - Error creando cliente: {}, error: {}", dto.nombre(), e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MensajeDto<ClienteDto>> actualizarCliente(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody ClienteDto dto) {
        try {
            log.info("👤 CLIENT_UPDATE - Iniciando actualización de cliente ID: {}", id);

            ClienteDto clienteActualizado = clienteService.actualizarCliente(id, dto);

            log.info("✅ CLIENT_UPDATE_SUCCESS - Cliente actualizado exitosamente: id={}, cedula={}",
                    id, clienteActualizado.cedula());

            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente actualizado exitosamente", clienteActualizado));
        } catch (Exception e) {
            log.error("❌ CLIENT_UPDATE_ERROR - Error actualizando cliente ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarCliente(@PathVariable("id") Long id) {
        try {
            log.info("👤 CLIENT_DELETE - Iniciando eliminación de cliente ID: {}", id);

            clienteService.eliminarCliente(id);

            log.info("✅ CLIENT_DELETE_SUCCESS - Cliente eliminado exitosamente: id={}", id);

            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente eliminado exitosamente"));
        } catch (Exception e) {
            log.error("❌ CLIENT_DELETE_ERROR - Error eliminando cliente ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ClienteDto>> obtenerClientePorId(@PathVariable("id") Long id) {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando cliente por ID: {}", id);

            ClienteDto dto = clienteService.obtenerClientePorId(id);

            log.info("✅ CLIENT_QUERY_SUCCESS - Cliente encontrado: id={}, nombre={}", id, dto.nombre());

            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente encontrado", dto));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error consultando cliente ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/cedula/{cedula}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ClienteDto>> obtenerClientePorCedula(@PathVariable("cedula") String cedula) {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando cliente por cédula: {}", cedula);

            ClienteDto dto = clienteService.obtenerClientePorCedula(cedula);

            log.info("✅ CLIENT_QUERY_SUCCESS - Cliente encontrado por cédula: cedula={}, nombre={}",
                    cedula, dto.nombre());

            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente encontrado por cédula", dto));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error consultando cliente cédula: {}, error: {}", cedula, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/correo/{correo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<ClienteDto>> obtenerClientePorCorreo(@PathVariable("correo") String correo) {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando cliente por correo: {}", correo);

            ClienteDto dto = clienteService.obtenerClientePorCorreo(correo);

            log.info("✅ CLIENT_QUERY_SUCCESS - Cliente encontrado por correo: correo={}, nombre={}",
                    correo, dto.nombre());

            return ResponseEntity.ok(new MensajeDto<>(false, "Cliente encontrado por correo", dto));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error consultando cliente correo: {}, error: {}", correo, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesPorTipo(@PathVariable("tipo") String tipo) {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando clientes por tipo: {}", tipo);

            TipoCliente tipoCliente = TipoCliente.valueOf(tipo.toUpperCase());
            List<ClienteDto> lista = clienteService.obtenerClientesPorTipo(tipoCliente);

            log.info("✅ CLIENT_QUERY_SUCCESS - Clientes por tipo encontrados: tipo={}, cantidad={}",
                    tipo, lista.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes por tipo obtenidos", lista));
        } catch (IllegalArgumentException iae) {
            log.warn("⚠️ CLIENT_QUERY_WARN - Tipo de cliente inválido: {}", tipo);
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Tipo de cliente inválido. Usa: NATURAL, JURIDICO", null));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error consultando clientes por tipo: {}, error: {}", tipo, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesPorNombre(@PathVariable("nombre") String nombre) {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando clientes por nombre: {}", nombre);

            List<ClienteDto> lista = clienteService.obtenerClientesPorNombre(nombre);

            log.info("✅ CLIENT_QUERY_SUCCESS - Clientes por nombre encontrados: nombre={}, cantidad={}",
                    nombre, lista.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes por nombre obtenidos", lista));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error consultando clientes por nombre: {}, error: {}", nombre, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/registrados-despues/{fecha}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesRegistradosDespuesDe(
            @PathVariable("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando clientes registrados después de: {}", fecha);

            List<ClienteDto> lista = clienteService.obtenerClientesRegistradosDespuesDe(fecha);

            log.info("✅ CLIENT_QUERY_SUCCESS - Clientes registrados después de fecha: fecha={}, cantidad={}",
                    fecha, lista.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes registrados después de la fecha obtenidos", lista));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error consultando clientes después de fecha: {}, error: {}", fecha, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/registrados-entre")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerClientesRegistradosEntre(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando clientes registrados entre: {} y {}", inicio, fin);

            List<ClienteDto> lista = clienteService.obtenerClientesRegistradosEntre(inicio, fin);

            log.info("✅ CLIENT_QUERY_SUCCESS - Clientes registrados en rango: inicio={}, fin={}, cantidad={}",
                    inicio, fin, lista.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Clientes registrados en el rango obtenidos", lista));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error consultando clientes en rango: {}-{}, error: {}",
                    inicio, fin, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    @GetMapping("/contar/tipo/{tipo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<Long>> contarClientesPorTipo(@PathVariable("tipo") String tipo) {
        try {
            log.info("🔢 CLIENT_COUNT - Contando clientes por tipo: {}", tipo);

            TipoCliente tipoCliente = TipoCliente.valueOf(tipo.toUpperCase());
            long cantidad = clienteService.contarClientesPorTipo(tipoCliente);

            log.info("✅ CLIENT_COUNT_SUCCESS - Conteo por tipo completado: tipo={}, cantidad={}", tipo, cantidad);

            return ResponseEntity.ok(new MensajeDto<>(false, "Conteo de clientes por tipo", cantidad));
        } catch (IllegalArgumentException iae) {
            log.warn("⚠️ CLIENT_COUNT_WARN - Tipo de cliente inválido: {}", tipo);
            return ResponseEntity.badRequest().body(new MensajeDto<>(true,
                    "Tipo de cliente inválido. Usa: NATURAL, JURIDICO", 0L));
        } catch (Exception e) {
            log.error("❌ CLIENT_COUNT_ERROR - Error contando clientes por tipo: {}, error: {}", tipo, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), 0L));
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<List<ClienteDto>>> obtenerTodosLosClientes() {
        try {
            log.info("🔍 CLIENT_QUERY - Consultando todos los clientes");

            List<ClienteDto> lista = clienteService.obtenerTodosLosClientes();

            log.info("✅ CLIENT_QUERY_SUCCESS - Todos los clientes obtenidos: cantidad={}", lista.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Todos los clientes obtenidos exitosamente", lista));
        } catch (Exception e) {
            log.error("❌ CLIENT_QUERY_ERROR - Error obteniendo todos los clientes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }
}