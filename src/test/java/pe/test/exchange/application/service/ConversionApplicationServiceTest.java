package pe.test.exchange.application.service;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.exception.MonedaNoSoportadaException;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.repository.ConversionRepository;
import pe.test.exchange.domain.service.TipoCambioDomainService;
import pe.test.exchange.infrastructure.external.ExchangeRateApiClient;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConversionApplicationServiceTest {

    private static final String API_KEY = "test-key";

    private final ExchangeRateApiClient apiClient = mock(ExchangeRateApiClient.class);
    private final ConversionRepository repository = mock(ConversionRepository.class);
    private final ConversionApplicationService service =
            new ConversionApplicationService(
                    apiClient,
                    repository,
                    new TipoCambioDomainService(),
                    API_KEY
            );

    @Test
    void convertsUsdToPenUsingPenRateFromUsdBaseResponse() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(successfulRates("USD", Map.of(
                        "PEN", new BigDecimal("3.70"),
                        "EUR", new BigDecimal("0.92")
                )));

        Conversion conversion = convert("100", "USD", "PEN");

        assertEquals(new BigDecimal("3.7000000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("370.00"), conversion.getMontoConvertido());
        verify(apiClient).getLatestRates(API_KEY, "USD");
    }

    @Test
    void convertsPenToUsdUsingUsdRateFromPenBaseResponse() {
        when(apiClient.getLatestRates(API_KEY, "PEN"))
                .thenReturn(successfulRates("PEN", Map.of(
                        "USD", new BigDecimal("0.27"),
                        "EUR", new BigDecimal("0.25")
                )));

        Conversion conversion = convert("100", "PEN", "USD");

        assertEquals(new BigDecimal("0.2700000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("27.00"), conversion.getMontoConvertido());
        verify(apiClient).getLatestRates(API_KEY, "PEN");
    }

    @Test
    void convertsEurToPenUsingPenRateFromEurBaseResponse() {
        when(apiClient.getLatestRates(API_KEY, "EUR"))
                .thenReturn(successfulRates("EUR", Map.of(
                        "PEN", new BigDecimal("4.01"),
                        "USD", new BigDecimal("1.08")
                )));

        Conversion conversion = convert("10", "EUR", "PEN");

        assertEquals(new BigDecimal("4.0100000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("40.10"), conversion.getMontoConvertido());
        verify(apiClient).getLatestRates(API_KEY, "EUR");
    }

    @Test
    void convertsSameCurrencyAtOneWithoutCallingProvider() {
        Conversion conversion = convert("12.50", "usd", "USD");

        assertEquals(new BigDecimal("1.0000000000"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("12.50"), conversion.getMontoConvertido());
        verifyNoInteractions(apiClient);
        verify(repository).save(any(Conversion.class));
    }

    @Test
    void rejectsUnavailableDestinationRate() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(successfulRates("USD", Map.of("EUR", BigDecimal.ONE)));

        assertThrows(
                MonedaNoSoportadaException.class,
                () -> service.convertir(
                        new BigDecimal("10"),
                        "USD",
                        "PEN"
                )
        );

        verify(repository, never()).save(any(Conversion.class));
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

    @Test
    void rejectsNonPositiveDestinationRate() {
        when(apiClient.getLatestRates(API_KEY, "USD"))
                .thenReturn(successfulRates("USD", Map.of("PEN", BigDecimal.ZERO)));

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

    private ExchangeRateApiResponse successfulRates(
            String baseCurrency,
            Map<String, BigDecimal> rates
    ) {
        return new ExchangeRateApiResponse("success", baseCurrency, rates);
    }

    private void assertInvalidProviderResponse() {
        assertThrows(
                IllegalStateException.class,
                () -> service.convertir(
                        new BigDecimal("10"),
                        "USD",
                        "PEN"
                )
        );

        verify(repository, never()).save(any(Conversion.class));
    }
}
