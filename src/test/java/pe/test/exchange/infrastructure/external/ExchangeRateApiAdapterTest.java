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
                        "USD",
                        Map.of("PEN", new BigDecimal("3.70"))
                ));

        assertInvalidProviderResponse();
    }

    @Test
    void rejectsProviderResponseWithoutRates() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(new ExchangeRateApiResponse("success", "USD", null));

        assertInvalidProviderResponse();
    }

    private void assertInvalidProviderResponse() {
        assertThrows(
                IllegalStateException.class,
                () -> adapter.obtenerTipoCambio("USD", "PEN")
        );
    }
}
