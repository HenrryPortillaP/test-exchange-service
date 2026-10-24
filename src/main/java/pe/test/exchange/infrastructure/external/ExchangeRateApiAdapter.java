package pe.test.exchange.infrastructure.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.test.exchange.application.port.TipoCambioProvider;
import pe.test.exchange.domain.model.Moneda;
import pe.test.exchange.domain.service.TipoCambioDomainService;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;

@Component
public class ExchangeRateApiAdapter implements TipoCambioProvider {

    private final ExchangeRateApiClient apiClient;
    private final TipoCambioDomainService tipoCambioService;
    private final String apiKey;

    public ExchangeRateApiAdapter(
            ExchangeRateApiClient apiClient,
            TipoCambioDomainService tipoCambioService,
            @Value("${exchange-rate.api.key}") String apiKey
    ) {
        this.apiClient = apiClient;
        this.tipoCambioService = tipoCambioService;
        this.apiKey = apiKey;
    }

    @Override
    public BigDecimal obtenerTipoCambio(
            String monedaOrigen,
            String monedaDestino
    ) {
        ExchangeRateApiResponse respuesta =
                apiClient.getLatestRates(apiKey, monedaOrigen);

        if (respuesta == null
                || !"success".equalsIgnoreCase(respuesta.result())
                || respuesta.conversionRates() == null) {
            throw new IllegalStateException(
                    "No se pudieron obtener las tasas de cambio"
            );
        }

        return tipoCambioService.calcularTipoCambio(
                respuesta.conversionRates(),
                new Moneda(monedaDestino)
        );
    }
}
