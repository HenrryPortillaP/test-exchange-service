package pe.test.exchange.domain.service;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.exception.MonedaNoSoportadaException;
import pe.test.exchange.domain.model.Moneda;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TipoCambioDomainServiceTest {

    private final TipoCambioDomainService service = new TipoCambioDomainService();

    @Test
    void returnsDestinationRateForUsdBase() {
        BigDecimal rate = service.calcularTipoCambio(
                Map.of("PEN", new BigDecimal("3.7000")),
                new Moneda("PEN")
        );

        assertEquals(new BigDecimal("3.7000"), rate);
    }

    @Test
    void returnsDestinationRateForPenBase() {
        BigDecimal rate = service.calcularTipoCambio(
                Map.of("USD", new BigDecimal("0.2703")),
                new Moneda("USD")
        );

        assertEquals(new BigDecimal("0.2703"), rate);
    }

    @Test
    void returnsDestinationRateForEurBase() {
        BigDecimal rate = service.calcularTipoCambio(
                Map.of("PEN", new BigDecimal("4.0100")),
                new Moneda("PEN")
        );

        assertEquals(new BigDecimal("4.0100"), rate);
    }

    @Test
    void rejectsMissingDestinationRate() {
        assertThrows(
                MonedaNoSoportadaException.class,
                () -> service.calcularTipoCambio(
                        Map.of("USD", BigDecimal.ONE),
                        new Moneda("PEN")
                )
        );
    }
}
