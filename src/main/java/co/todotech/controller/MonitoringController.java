package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.enums.EstadoOrden;
import co.todotech.service.impl.OrdenServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {

    private final OrdenServiceImpl ordenService;

    /**
     * ✅ ENDPOINT PARA VER ESTADO DE UNA ORDEN ESPECÍFICA
     * URL: GET /api/monitoring/ordenes/{id}/estado
     */
    @GetMapping("/ordenes/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR') or hasRole('CAJERO')")
    public ResponseEntity<MensajeDto<Map<String, Object>>> getEstadoOrden(@PathVariable("id") Long id) {
        try {
            log.info("MONITORING_API - Consultando estado de orden: {}", id);

            Map<String, Object> status = ordenService.getOrdenStatusForMonitoring(id);

            if (status.containsKey("error")) {
                return ResponseEntity.status(404)
                        .body(new MensajeDto<>(true, "Orden no encontrada", status));
            }

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Estado de orden obtenido exitosamente", status));

        } catch (Exception e) {
            log.error("MONITORING_API_ERROR - Error consultando orden {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al consultar orden: " + e.getMessage(), null));
        }
    }

    /**
     * ✅ ENDPOINT PARA REPORTE GENERAL
     * URL: GET /api/monitoring/ordenes/summary
     */
    @GetMapping("/ordenes/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<Map<String, Object>>> getOrdenesSummary() {
        try {
            log.info("MONITORING_API - Generando resumen de órdenes");

            Map<String, Object> summary = ordenService.getOrdenesSummaryForMonitoring();

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Resumen de órdenes generado exitosamente", summary));

        } catch (Exception e) {
            log.error("MONITORING_API_ERROR - Error generando resumen: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al generar resumen: " + e.getMessage(), null));
        }
    }

    /**
     * ✅ ENDPOINT PARA ORDENES POR ESTADO
     * URL: GET /api/monitoring/ordenes/estado/{estado}
     */
    @GetMapping("/ordenes/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<MensajeDto<List<Map<String, Object>>>> getOrdenesPorEstado(
            @PathVariable("estado") String estado) {
        try {
            log.info("MONITORING_API - Consultando órdenes por estado: {}", estado);

            EstadoOrden estadoEnum = EstadoOrden.valueOf(estado.toUpperCase());
            List<Map<String, Object>> ordenes = ordenService.getOrdenesPorEstadoForMonitoring(estadoEnum);

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false,
                            "Órdenes por estado obtenidas exitosamente",
                            ordenes));

        } catch (IllegalArgumentException e) {
            log.warn("MONITORING_API_WARN - Estado inválido: {}", estado);
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Estado inválido: " + estado, null));
        } catch (Exception e) {
            log.error("MONITORING_API_ERROR - Error consultando órdenes por estado: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al consultar órdenes: " + e.getMessage(), null));
        }
    }

    /**
     * ✅ ENDPOINT PARA ORDENES PAGADAS
     * URL: GET /api/monitoring/ordenes/pagadas
     */
    @GetMapping("/ordenes/pagadas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR') or hasRole('CAJERO')")
    public ResponseEntity<MensajeDto<List<Map<String, Object>>>> getOrdenesPagadas() {
        try {
            log.info("MONITORING_API - Consultando órdenes pagadas");

            List<Map<String, Object>> ordenesPagadas =
                    ordenService.getOrdenesPorEstadoForMonitoring(EstadoOrden.PAGADA);

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false,
                            "Órdenes pagadas obtenidas exitosamente",
                            ordenesPagadas));

        } catch (Exception e) {
            log.error("MONITORING_API_ERROR - Error consultando órdenes pagadas: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al consultar órdenes pagadas: " + e.getMessage(), null));
        }
    }

    /**
     * ✅ ENDPOINT PARA ORDENES DISPONIBLES PARA PAGO
     * URL: GET /api/monitoring/ordenes/disponibles-pago
     */
    @GetMapping("/ordenes/disponibles-pago")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR') or hasRole('CAJERO')")
    public ResponseEntity<MensajeDto<List<Map<String, Object>>>> getOrdenesDisponiblesParaPago() {
        try {
            log.info("MONITORING_API - Consultando órdenes disponibles para pago");

            List<Map<String, Object>> ordenesDisponibles =
                    ordenService.getOrdenesPorEstadoForMonitoring(EstadoOrden.DISPONIBLEPARAPAGO);

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false,
                            "Órdenes disponibles para pago obtenidas exitosamente",
                            ordenesDisponibles));

        } catch (Exception e) {
            log.error("MONITORING_API_ERROR - Error consultando órdenes disponibles: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al consultar órdenes disponibles: " + e.getMessage(), null));
        }
    }

    /**
     * ✅ ENDPOINT PARA ESTADÍSTICAS RÁPIDAS
     * URL: GET /api/monitoring/estadisticas
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<Map<String, Object>>> getEstadisticas() {
        try {
            log.info("MONITORING_API - Generando estadísticas rápidas");

            Map<String, Object> summary = ordenService.getOrdenesSummaryForMonitoring();

            // Extraer solo las estadísticas clave para un vistazo rápido
            Map<String, Object> estadisticas = Map.of(
                    "totalOrdenes", summary.get("totalOrdenes"),
                    "totalPagadas", summary.get("totalPagadas"),
                    "totalVentas", summary.get("totalVentas"),
                    "timestamp", summary.get("timestamp")
            );

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Estadísticas obtenidas exitosamente", estadisticas));

        } catch (Exception e) {
            log.error("MONITORING_API_ERROR - Error generando estadísticas: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al generar estadísticas: " + e.getMessage(), null));
        }
    }

    /**
     * ✅ ENDPOINT PARA VERIFICAR SALUD DEL MONITOREO
     * URL: GET /api/monitoring/health
     */
    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<Map<String, Object>>> getHealthCheck() {
        try {
            log.info("MONITORING_API - Verificando salud del sistema de monitoreo");

            Map<String, Object> health = Map.of(
                    "status", "healthy",
                    "service", "monitoring",
                    "timestamp", java.time.LocalDateTime.now().toString(),
                    "endpointsAvailable", List.of(
                            "/api/monitoring/ordenes/{id}/estado",
                            "/api/monitoring/ordenes/summary",
                            "/api/monitoring/ordenes/pagadas",
                            "/api/monitoring/estadisticas"
                    )
            );

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Sistema de monitoreo funcionando correctamente", health));

        } catch (Exception e) {
            log.error("MONITORING_API_ERROR - Error en health check: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MensajeDto<>(true, "Error en sistema de monitoreo: " + e.getMessage(), null));
        }
    }
}