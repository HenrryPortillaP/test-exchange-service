package pe.test.exchange.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Conversion {

    private final String id;
    private final Moneda monedaOrigen;
    private final Moneda monedaDestino;
    private final BigDecimal monto;
    private final BigDecimal tipoCambio;
    private final BigDecimal montoConvertido;
    private final LocalDateTime fecha;

    public Conversion(
            String id,
            Moneda monedaOrigen,
            Moneda monedaDestino,
            BigDecimal monto,
            BigDecimal tipoCambio,
            LocalDateTime fecha
    ) {
        this(
                id,
                monedaOrigen,
                monedaDestino,
                monto,
                tipoCambio,
                calcularMontoConvertido(monto, tipoCambio),
                fecha
        );
    }

    public Conversion(
            String id,
            Moneda monedaOrigen,
            Moneda monedaDestino,
            BigDecimal monto,
            BigDecimal tipoCambio,
            BigDecimal montoConvertido,
            LocalDateTime fecha
    ) {

        if (monto == null) {
            throw new IllegalArgumentException("El monto es obligatorio");
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }

        if (tipoCambio == null) {
            throw new IllegalArgumentException("El tipo de cambio es obligatorio");
        }

        if (tipoCambio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El tipo de cambio debe ser mayor que cero");
        }

        if (montoConvertido == null) {
            throw new IllegalArgumentException("El monto convertido es obligatorio");
        }

        if (montoConvertido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto convertido debe ser mayor que cero"
            );
        }

        BigDecimal montoNormalizado = monto.setScale(6, RoundingMode.HALF_UP);
        BigDecimal tipoCambioNormalizado =
                tipoCambio.setScale(10, RoundingMode.HALF_UP);

        if (montoNormalizado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero después del redondeo"
            );
        }

        if (tipoCambioNormalizado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El tipo de cambio debe ser mayor que cero después del redondeo"
            );
        }

        this.id = id;
        this.monedaOrigen = Objects.requireNonNull(
                monedaOrigen, "La moneda de origen es obligatoria"
        );

        this.monedaDestino = Objects.requireNonNull(
                monedaDestino, "La moneda de destino es obligatoria"
        );
        this.monto = montoNormalizado;
        this.tipoCambio = tipoCambioNormalizado;
        this.montoConvertido = montoConvertido;
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
    }

    private static BigDecimal calcularMontoConvertido(
            BigDecimal monto,
            BigDecimal tipoCambio
    ) {
        if (monto == null || tipoCambio == null) {
            return null;
        }

        return monto.setScale(6, RoundingMode.HALF_UP)
                .multiply(tipoCambio.setScale(10, RoundingMode.HALF_UP))
                .setScale(6, RoundingMode.HALF_UP);
    }
}