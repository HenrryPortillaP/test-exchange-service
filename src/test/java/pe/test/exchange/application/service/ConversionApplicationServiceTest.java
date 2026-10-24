package pe.test.exchange.application.service;

import org.junit.jupiter.api.Test;
import pe.test.exchange.application.port.TipoCambioProvider;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.repository.ConversionRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConversionApplicationServiceTest {

    private final TipoCambioProvider tipoCambioProvider =
            mock(TipoCambioProvider.class);
    private final ConversionRepository repository = mock(ConversionRepository.class);
    private final ConversionApplicationService service =
            new ConversionApplicationService(
                    tipoCambioProvider,
                    repository
            );

    @Test
    void convertsUsdToPenUsingRateFromProvider() {
        when(tipoCambioProvider.obtenerTipoCambio("USD", "PEN"))
                .thenReturn(new BigDecimal("3.70"));

        Conversion conversion = convert("100", "USD", "PEN");

        assertEquals(new BigDecimal("3.7000000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("370.000000"), conversion.getMontoConvertido());
        verify(tipoCambioProvider).obtenerTipoCambio("USD", "PEN");
    }

    @Test
    void convertsPenToUsdUsingRateFromProvider() {
        when(tipoCambioProvider.obtenerTipoCambio("PEN", "USD"))
                .thenReturn(new BigDecimal("0.27"));

        Conversion conversion = convert("100", "PEN", "USD");

        assertEquals(new BigDecimal("0.2700000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("27.000000"), conversion.getMontoConvertido());
        verify(tipoCambioProvider).obtenerTipoCambio("PEN", "USD");
    }

    @Test
    void convertsEurToPenUsingRateFromProvider() {
        when(tipoCambioProvider.obtenerTipoCambio("EUR", "PEN"))
                .thenReturn(new BigDecimal("4.01"));

        Conversion conversion = convert("10", "EUR", "PEN");

        assertEquals(new BigDecimal("4.0100000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("40.100000"), conversion.getMontoConvertido());
        verify(tipoCambioProvider).obtenerTipoCambio("EUR", "PEN");
    }

    @Test
    void convertsSameCurrencyAtOneWithoutCallingProvider() {
        Conversion conversion = convert("12.50", "usd", "USD");

        assertEquals(new BigDecimal("1.0000000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("12.500000"), conversion.getMontoConvertido());
        verifyNoInteractions(tipoCambioProvider);
        verify(repository).save(any(Conversion.class));
    }

    @Test
    void rejectsUnavailableDestinationRate() {
        when(tipoCambioProvider.obtenerTipoCambio("USD", "PEN"))
                .thenThrow(new IllegalArgumentException(
                        "Moneda no soportada: PEN"
                ));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.convertir(
                        new BigDecimal("10"),
                        "USD",
                        "PEN"
                )
        );

        verify(repository, never()).save(any(Conversion.class));
    }

    @Test
    void rejectsNonPositiveDestinationRate() {
        when(tipoCambioProvider.obtenerTipoCambio("USD", "PEN"))
                .thenReturn(BigDecimal.ZERO);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.convertir(
                        new BigDecimal("10"),
                        "USD",
                        "PEN"
                )
        );

        verify(repository, never()).save(any(Conversion.class));
    }

    private Conversion convert(String amount, String source, String target) {
        when(repository.save(any(Conversion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        return service.convertir(new BigDecimal(amount), source, target);
    }
}
