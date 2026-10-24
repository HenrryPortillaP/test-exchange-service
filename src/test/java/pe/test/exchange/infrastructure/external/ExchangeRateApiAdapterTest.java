package pe.test.exchange.infrastructure.external;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.service.TipoCambioDomainService;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExchangeRateApiAdapterTest {

    private static final String API_KEY = "test-key";

    private final ExchangeRateApiClient apiClient =
            mock(ExchangeRateApiClient.class);
    private final ExchangeRateApiAdapter adapter = new ExchangeRateApiAdapter(
            apiClient,
            new TipoCambioDomainService(),
            API_KEY
    );

    @Test
    void returnsRequestedRateFromProviderResponse() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(new ExchangeRateApiResponse(
                        "success",
                        null,
                        "USD",
                        Map.of(
                                "PEN", new BigDecimal("3.70"),
                                "EUR", new BigDecimal("0.92")
                        )
                ));

        BigDecimal rate = adapter.obtenerTipoCambio("USD", "PEN");

        assertEquals(new BigDecimal("3.70"), rate);
        verify(apiClient).getLatestRates(API_KEY, "USD");
    }

    @Test
    void rejectsNullProviderResponse() {
        when(apiClient.getLatestRates(API_KEY, "USD")).thenReturn(null);

        assertInvalidProviderResponse();
    }

    @Test
    void rejectsFailedProviderResponse() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(new ExchangeRateApiResponse(
                        "error",
                        "unknown-error",
                        "USD",
                        Map.of("PEN", new BigDecimal("3.70"))
                ));

        assertInvalidProviderResponse();
    }

    @Test
    void rejectsProviderResponseWithoutRates() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(new ExchangeRateApiResponse(
                        "success",
                        null,
                        "USD",
                        null
                ));

        assertInvalidProviderResponse();
    }

    @Test
    void reportsInvalidApiKeySpecifically() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(new ExchangeRateApiResponse(
                        "error",
                        "invalid-key",
                        null,
                        null
                ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> adapter.obtenerTipoCambio("USD", "PEN")
        );

        assertEquals(
                "La clave API del proveedor de tipos de cambio no es válida.",
                exception.getMessage()
        );
    }

    @Test
    void reportsUnsupportedBaseCurrencySpecifically() {
        when(apiClient.getLatestRates(API_KEY, "USA"))
                .thenReturn(new ExchangeRateApiResponse(
                        "error",
                        "unsupported-code",
                        null,
                        null
                ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.obtenerTipoCambio("USA", "PEN")
        );

        assertEquals(
                "La moneda base no es compatible con el proveedor: USA",
                exception.getMessage()
        );
    }

    private void assertInvalidProviderResponse() {
        assertThrows(
                IllegalStateException.class,
                () -> adapter.obtenerTipoCambio("USD", "PEN")
        );
    }
}
