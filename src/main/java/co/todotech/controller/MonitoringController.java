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

    //COMENTANDO
    private final OrdenServiceImpl ordenService;

    /**
     * ✅ ENDPOINT PÚBLICO PARA HEALTH CHECK
     * URL: GET /api/monitoring/health
     */
    @GetMapping("/health")
    public ResponseEntity<MensajeDto<Map<String, Object>>> monitoringHealth() {
        try {
            log.info("🔴 MONITORING_HEALTH - Health check solicitado desde Postman");
            log.info("🟢 MONITORING_SYSTEM - Sistema de monitoreo activo - CloudFront: https://d2jctboz5xbevf.cloudfront.net");

            Map<String, Object> health = Map.of(
                    "status", "healthy",
                    "service", "todo-tech-monitoring",
                    "cloudfront", "https://d2jctboz5xbevf.cloudfront.net",
                    "timestamp", java.time.LocalDateTime.now().toString(),
                    "endpoints", List.of(
                            "/api/monitoring/health",
                            "/api/monitoring/ordenes/{id}/estado",
                            "/api/monitoring/ordenes/summary",
                            "/api/monitoring/test"
                    )
            );

            return ResponseEntity.ok(new MensajeDto<>(false, "✅ Sistema de monitoreo funcionando correctamente", health));

        } catch (Exception e) {
            log.error("❌ MONITORING_HEALTH_ERROR - Error en health check: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MensajeDto<>(true, "Error en sistema de monitoreo", null));
        }
    }

    /**
     * ✅ ENDPOINT PÚBLICO DE PRUEBA
     * URL: GET /api/monitoring/test
     */
    @GetMapping("/test")
    public ResponseEntity<MensajeDto<Map<String, String>>> monitoringTest() {
        log.info("🧪 MONITORING_TEST_INICIO - Endpoint de prueba ejecutado desde Postman");
        log.info("📝 MONITORING_TEST_DETALLE - orderId: 999, status: TEST, paid: true, amount: 150.75");
        log.info("✅ MONITORING_TEST_FIN - Prueba completada exitosamente");

        Map<String, String> response = Map.of(
                "status", "success",
                "message", "✅ Prueba de monitoreo exitosa - Los logs están funcionando",
                "cloudfront", "https://d2jctboz5xbevf.cloudfront.net",
                "timestamp", java.time.LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(new MensajeDto<>(false, "Prueba de monitoreo exitosa", response));
    }

    /**
     * ✅ ENDPOINT PARA VER ESTADO DE UNA ORDEN ESPECÍFICA
     * URL: GET /api/monitoring/ordenes/{id}/estado
     */
    @GetMapping("/ordenes/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR') or hasRole('CAJERO')")
    public ResponseEntity<MensajeDto<Map<String, Object>>> getEstadoOrden(@PathVariable("id") Long id) {
        try {
            log.info("📊 MONITORING_ORDER_STATUS - Consultando estado de orden: {}", id);

            Map<String, Object> status = ordenService.getOrdenStatusForMonitoring(id);

            if (status.containsKey("error")) {
                log.warn("⚠️ MONITORING_ORDER_NOT_FOUND - Orden no encontrada: {}", id);
                return ResponseEntity.status(404)
                        .body(new MensajeDto<>(true, "Orden no encontrada", status));
            }

            log.info("✅ MONITORING_ORDER_SUCCESS - Orden: {}, Estado: {}, Pagada: {}, Total: {}",
                    id, status.get("estado"), status.get("pagada"), status.get("total"));

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Estado de orden obtenido exitosamente", status));

        } catch (Exception e) {
            log.error("❌ MONITORING_API_ERROR - Error consultando orden {}: {}", id, e.getMessage());
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
            log.info("📈 MONITORING_SUMMARY - Generando resumen de órdenes");

            Map<String, Object> summary = ordenService.getOrdenesSummaryForMonitoring();

            log.info("✅ MONITORING_SUMMARY_SUCCESS - Total órdenes: {}, Pagadas: {}, Ventas: {}",
                    summary.get("totalOrdenes"), summary.get("totalPagadas"), summary.get("totalVentas"));

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Resumen de órdenes generado exitosamente", summary));

        } catch (Exception e) {
            log.error("❌ MONITORING_API_ERROR - Error generando resumen: {}", e.getMessage());
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
            log.info("🔍 MONITORING_ORDERS_BY_STATUS - Consultando órdenes por estado: {}", estado);

            EstadoOrden estadoEnum = EstadoOrden.valueOf(estado.toUpperCase());
            List<Map<String, Object>> ordenes = ordenService.getOrdenesPorEstadoForMonitoring(estadoEnum);

            log.info("✅ MONITORING_ORDERS_BY_STATUS_SUCCESS - Estado: {}, Cantidad: {}", estado, ordenes.size());

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Órdenes por estado obtenidas exitosamente", ordenes));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ MONITORING_API_WARN - Estado inválido: {}", estado);
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Estado inválido: " + estado, null));
        } catch (Exception e) {
            log.error("❌ MONITORING_API_ERROR - Error consultando órdenes por estado: {}", e.getMessage());
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
            log.info("💰 MONITORING_PAID_ORDERS - Consultando órdenes pagadas");

            List<Map<String, Object>> ordenesPagadas =
                    ordenService.getOrdenesPorEstadoForMonitoring(EstadoOrden.PAGADA);

            log.info("✅ MONITORING_PAID_ORDERS_SUCCESS - Órdenes pagadas encontradas: {}", ordenesPagadas.size());

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Órdenes pagadas obtenidas exitosamente", ordenesPagadas));

        } catch (Exception e) {
            log.error("❌ MONITORING_API_ERROR - Error consultando órdenes pagadas: {}", e.getMessage());
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
            log.info("🛒 MONITORING_AVAILABLE_ORDERS - Consultando órdenes disponibles para pago");

            List<Map<String, Object>> ordenesDisponibles =
                    ordenService.getOrdenesPorEstadoForMonitoring(EstadoOrden.DISPONIBLEPARAPAGO);

            log.info("✅ MONITORING_AVAILABLE_ORDERS_SUCCESS - Órdenes disponibles: {}", ordenesDisponibles.size());

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Órdenes disponibles para pago obtenidas exitosamente", ordenesDisponibles));

        } catch (Exception e) {
            log.error("❌ MONITORING_API_ERROR - Error consultando órdenes disponibles: {}", e.getMessage());
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
            log.info("📊 MONITORING_STATS - Generando estadísticas rápidas");

            Map<String, Object> summary = ordenService.getOrdenesSummaryForMonitoring();

            Map<String, Object> estadisticas = Map.of(
                    "totalOrdenes", summary.get("totalOrdenes"),
                    "totalPagadas", summary.get("totalPagadas"),
                    "totalVentas", summary.get("totalVentas"),
                    "timestamp", summary.get("timestamp")
            );

            log.info("✅ MONITORING_STATS_SUCCESS - Estadísticas generadas");

            return ResponseEntity.ok()
                    .body(new MensajeDto<>(false, "Estadísticas obtenidas exitosamente", estadisticas));

        } catch (Exception e) {
            log.error("❌ MONITORING_API_ERROR - Error generando estadísticas: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al generar estadísticas: " + e.getMessage(), null));
        }
    }
}