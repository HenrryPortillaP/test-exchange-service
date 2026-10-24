package pe.test.exchange.domain.service;

import pe.test.exchange.domain.model.Moneda;

import java.math.BigDecimal;
import java.util.Map;

public class TipoCambioDomainService {

    public BigDecimal calcularTipoCambio(
            Map<String, BigDecimal> tasas,
            Moneda monedaDestino
    ) {
        BigDecimal tasaDestino = tasas.get(monedaDestino.codigo());

        if (tasaDestino == null) {
            throw new IllegalArgumentException(
                    "Moneda no soportada: " + monedaDestino.codigo()
            );
        }

        if (tasaDestino.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Las tasas deben ser mayores que cero"
            );
        }

        return tasaDestino;
    }
}