package com.multipagos.p2p_backend.backend.service; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Comision;
import com.multipagos.p2p_backend.backend.model.Transaccion;
import com.multipagos.p2p_backend.backend.repository.ComisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ComisionService {

    private final ComisionRepository comisionRepository;

    public ComisionService(ComisionRepository comisionRepository) {
        this.comisionRepository = comisionRepository;
    }

    @Transactional
    public Comision registrarComision(Transaccion transaccion) {
        System.out.println("DEBUG: ComisionService - Iniciando registro de comisión para Transacción ID: " + transaccion.getId());
        System.out.println("DEBUG: ComisionService - Estado de la transacción recibida: " + transaccion.getEstado());
        System.out.println("DEBUG: ComisionService - Comisión Bruta recibida: " + transaccion.getComisionBruta());

        // Validar que la transacción esté completada
        if (!transaccion.getEstado().equalsIgnoreCase("completada")) {
            System.err.println("ERROR: ComisionService - La transacción no está completada para registrar comisión. Estado: " + transaccion.getEstado());
            throw new IllegalArgumentException("Solo se puede registrar comisión para transacciones completadas.");
        }

        // Recuperar la comisión bruta de la transacción (ya calculada en TransaccionService)
        BigDecimal montoComisionBruta = transaccion.getComisionBruta();
        System.out.println("DEBUG: ComisionService - Monto Comisión Bruta: " + montoComisionBruta);

        // --- Definir porcentajes de reparto (¡Estos deberían ser configurables por el Admin en producción!) ---
        BigDecimal porcentajePlataforma = new BigDecimal("0.50"); // 50% para la plataforma
        BigDecimal porcentajeVendedor = new BigDecimal("0.30");   // 30% para el vendedor
        BigDecimal porcentajeCajero = new BigDecimal("0.20");     // 20% para el cajero

        // Calcular los montos de reparto
        BigDecimal montoPlataforma = montoComisionBruta.multiply(porcentajePlataforma).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoVendedor = montoComisionBruta.multiply(porcentajeVendedor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoCajero = montoComisionBruta.multiply(porcentajeCajero).setScale(2, RoundingMode.HALF_UP);

        System.out.println("DEBUG: ComisionService - Montos calculados: Plataforma=" + montoPlataforma + ", Vendedor=" + montoVendedor + ", Cajero=" + montoCajero);

        // Crear la entidad Comision
        Comision comision = new Comision();
        comision.setTransaccion(transaccion);
        comision.setMontoComisionBruta(montoComisionBruta);
        comision.setPorcentajePlataforma(porcentajePlataforma);
        comision.setMontoPlataforma(montoPlataforma);
        comision.setPorcentajeVendedor(porcentajeVendedor);
        comision.setMontoVendedor(montoVendedor);
        comision.setPorcentajeCajero(porcentajeCajero);
        comision.setMontoCajero(montoCajero);
        // Las fechas de creación/actualización se manejan en @PrePersist

        // Guardar la comisión en la base de datos
        Comision savedComision = comisionRepository.save(comision);
        System.out.println("DEBUG: ComisionService - Comisión guardada en DB con ID: " + savedComision.getId());
        
        return savedComision;
    }

    // Puedes añadir métodos para obtener reportes de comisiones aquí
    // @Transactional(readOnly = true)
    // public List<Comision> obtenerComisionesPorUsuario(Long userId) { ... }
    // public BigDecimal obtenerTotalComisionesPlataforma() { ... }
}
