package pe.test.exchange;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Moneda;
import pe.test.exchange.domain.repository.ConversionRepository;
import pe.test.exchange.infrastructure.persistence.ConversionEntity;
import pe.test.exchange.infrastructure.persistence.SpringDataConversionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = "exchange-rate.api.key=test-key")
class ExchangeServiceApplicationTests {

    @Autowired
    private ConversionRepository conversionRepository;

    @Autowired
    private SpringDataConversionRepository jpaRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void rehydratedConversionUsesPersistedConvertedAmount() {
        String id = UUID.randomUUID().toString();
        BigDecimal persistedAmount = new BigDecimal("40.123456");
        jpaRepository.save(new ConversionEntity(
                id,
                "USD",
                "PEN",
                new BigDecimal("10.000000"),
                new BigDecimal("3.0000000000"),
                persistedAmount,
                LocalDateTime.now()
        ));

        Conversion rehydrated = conversionRepository.findById(id).orElseThrow();

        assertEquals(persistedAmount, rehydrated.getMontoConvertido());
    }

    @Test
    void conversionKeepsRoundedAmountConsistentAfterSaveAndHistoryRead() {
        Conversion conversion = new Conversion(
                UUID.randomUUID().toString(),
                new Moneda("USD"),
                new Moneda("PEN"),
                new BigDecimal("1.0049996"),
                new BigDecimal("1.00000000005"),
                LocalDateTime.now()
        );

        Conversion saved = conversionRepository.save(conversion);
        ConversionEntity persisted = jpaRepository.findById(conversion.getId())
                .orElseThrow();
        Conversion historical = conversionRepository.findAll()
                .stream()
                .filter(item -> item.getId().equals(conversion.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("1.005000"), persisted.getMonto());
        assertEquals(new BigDecimal("1.0000000001"), persisted.getTipoCambio());
        assertEquals(new BigDecimal("1.005000"), persisted.getMontoConvertido());
        assertEquals(persisted.getMontoConvertido(), saved.getMontoConvertido());
        assertEquals(persisted.getMontoConvertido(), historical.getMontoConvertido());
        assertEquals(
                persisted.getMontoConvertido(),
                ConversionResponse.from(saved).montoConvertido()
        );
        assertEquals(
                persisted.getMontoConvertido(),
                ConversionResponse.from(historical).montoConvertido()
        );
    }

    @Test
    void conversionUsesHalfUpAtEachConfiguredScale() {
        Conversion conversion = new Conversion(
                UUID.randomUUID().toString(),
                new Moneda("USD"),
                new Moneda("PEN"),
                new BigDecimal("1.2345675"),
                new BigDecimal("1.23456789015"),
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("1.234568"), conversion.getMonto());
        assertEquals(new BigDecimal("1.2345678902"), conversion.getTipoCambio());
        assertEquals(new BigDecimal("1.524158"), conversion.getMontoConvertido());
    }

    @Test
    void conversionRejectsAmountsThatRoundToZero() {
        assertThrows(IllegalArgumentException.class, () -> new Conversion(
                UUID.randomUUID().toString(),
                new Moneda("USD"),
                new Moneda("PEN"),
                new BigDecimal("0.0000004"),
                BigDecimal.ONE,
                LocalDateTime.now()
        ));
    }

    @Test
    void conversionRejectsRatesThatRoundToZero() {
        assertThrows(IllegalArgumentException.class, () -> new Conversion(
                UUID.randomUUID().toString(),
                new Moneda("USD"),
                new Moneda("PEN"),
                BigDecimal.ONE,
                new BigDecimal("0.00000000004"),
                LocalDateTime.now()
        ));
    }
}
