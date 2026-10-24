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

        if (respuesta == null) {
            throw new IllegalStateException(
                    "No se pudieron obtener las tasas de cambio"
            );
        }

        if ("error".equalsIgnoreCase(respuesta.result())) {
            throw errorProveedor(respuesta.errorType(), monedaOrigen);
        }

        if (!"success".equalsIgnoreCase(respuesta.result())
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

    private RuntimeException errorProveedor(String tipoError, String monedaOrigen) {
        if ("invalid-key".equalsIgnoreCase(tipoError)) {
            return new IllegalStateException(
                    "La clave API del proveedor de tipos de cambio no es válida."
            );
        }

        if ("unsupported-code".equalsIgnoreCase(tipoError)) {
            return new IllegalArgumentException(
                    "La moneda base no es compatible con el proveedor: "
                            + monedaOrigen
            );
        }

        return new IllegalStateException(
                "El proveedor de tipos de cambio respondió con un error"
        );
    }
}
