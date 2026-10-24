package pe.test.exchange.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.test.exchange.application.port.TipoCambioProvider;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Moneda;
import pe.test.exchange.domain.repository.ConversionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConversionApplicationService {

    private final TipoCambioProvider tipoCambioProvider;
    private final ConversionRepository conversionRepository;

    public ConversionApplicationService(
            TipoCambioProvider tipoCambioProvider,
            ConversionRepository conversionRepository
    ) {
        this.tipoCambioProvider = tipoCambioProvider;
        this.conversionRepository = conversionRepository;
    }

    @Transactional
    public Conversion convertir(
            BigDecimal monto,
            String codigoOrigen,
            String codigoDestino
    ) {
        Moneda origen = new Moneda(codigoOrigen);
        Moneda destino = new Moneda(codigoDestino);

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero"
            );
        }

        BigDecimal tipoCambio;
        if (origen.equals(destino)) {
            tipoCambio = BigDecimal.ONE;
        } else {
            tipoCambio = tipoCambioProvider.obtenerTipoCambio(
                    origen.codigo(),
                    destino.codigo()
            );
        }

        Conversion conversion = new Conversion(
                UUID.randomUUID().toString(),
                origen,
                destino,
                monto,
                tipoCambio,
                LocalDateTime.now()
        );

        return conversionRepository.save(conversion);
    }

    @Transactional(readOnly = true)
    public List<Conversion> listarConversiones() {
        return conversionRepository.findAll();
    }
}