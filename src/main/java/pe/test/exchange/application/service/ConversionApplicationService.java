package pe.test.exchange.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Moneda;
import pe.test.exchange.domain.repository.ConversionRepository;
import pe.test.exchange.domain.service.TipoCambioDomainService;
import pe.test.exchange.infrastructure.external.ExchangeRateApiClient;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConversionApplicationService {

    private final ExchangeRateApiClient apiClient;
    private final ConversionRepository conversionRepository;
    private final TipoCambioDomainService tipoCambioService;
    private final String apiKey;

    public ConversionApplicationService(
            ExchangeRateApiClient apiClient,
            ConversionRepository conversionRepository,
            TipoCambioDomainService tipoCambioService,
            @Value("${exchange-rate.api.key}") String apiKey
    ) {
        this.apiClient = apiClient;
        this.conversionRepository = conversionRepository;
        this.tipoCambioService = tipoCambioService;
        this.apiKey = apiKey;
    }

    @Transactional
    public Conversion convertir(
            BigDecimal monto,
            String codigoOrigen,
            String codigoDestino
    ) {
        Moneda origen = new Moneda(codigoOrigen);
        Moneda destino = new Moneda(codigoDestino);

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero"
            );
        }

        BigDecimal tipoCambio;
        if (origen.equals(destino)) {
            tipoCambio = BigDecimal.ONE;
        } else {
            ExchangeRateApiResponse respuesta =
                    apiClient.getLatestRates(apiKey, origen.codigo());

            if (respuesta == null
                    || !"success".equalsIgnoreCase(respuesta.result())
                    || respuesta.conversionRates() == null) {
                throw new IllegalStateException(
                        "No se pudieron obtener las tasas de cambio"
                );
            }

            tipoCambio = tipoCambioService.calcularTipoCambio(
                    respuesta.conversionRates(),
                    destino
            );
        }

        Conversion conversion = new Conversion(
                UUID.randomUUID().toString(),
                origen,
                destino,
                monto,
                tipoCambio,
                LocalDateTime.now()
        );

        return conversionRepository.save(conversion);
    }
}