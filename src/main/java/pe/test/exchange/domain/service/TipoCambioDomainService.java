package pe.test.exchange.domain.service;

import pe.test.exchange.domain.exception.MonedaNoSoportadaException;
import pe.test.exchange.domain.model.Moneda;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

public class TipoCambioDomainService {

    public BigDecimal calcularTipoCambio(
            Map<String, BigDecimal> tasas,
            Moneda monedaOrigen,
            Moneda monedaDestino
    ) {
        BigDecimal tasaOrigen = tasas.get(monedaOrigen.codigo());
        BigDecimal tasaDestino = tasas.get(monedaDestino.codigo());

        if (tasaOrigen == null) {
            throw new MonedaNoSoportadaException(monedaOrigen.codigo());
        }

        if (tasaDestino == null) {
            throw new MonedaNoSoportadaException(monedaDestino.codigo());
        }

        if (tasaOrigen.compareTo(BigDecimal.ZERO) <= 0
                || tasaDestino.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Las tasas deben ser mayores que cero"
            );
        }

        return tasaDestino.divide(tasaOrigen, MathContext.DECIMAL128);
    }
}