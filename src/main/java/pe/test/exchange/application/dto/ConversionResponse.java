package pe.test.exchange.application.dto;

import pe.test.exchange.domain.model.Conversion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConversionResponse(
        String id,
        BigDecimal monto,
        String monedaOrigen,
        String monedaDestino,
        BigDecimal tipoCambio,
        BigDecimal montoConvertido,
        LocalDateTime fecha
) {
    public static ConversionResponse from(Conversion conversion) {
        return new ConversionResponse(
                conversion.getId(),
                conversion.getMonto(),
                conversion.getMonedaOrigen().codigo(),
                conversion.getMonedaDestino().codigo(),
                conversion.getTipoCambio(),
                conversion.getMontoConvertido(),
                conversion.getFecha()
        );
    }
}