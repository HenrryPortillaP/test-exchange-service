package pe.test.exchange.infrastructure.persistence;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.AccessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "conversiones")
public class ConversionEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 3)
    private String monedaOrigen;

    @Column(nullable = false, length = 3)
    private String monedaDestino;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal monto;

    @Column(nullable = false, precision = 19, scale = 10)
    private BigDecimal tipoCambio;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montoConvertido;

    @Column(nullable = false)
    private LocalDateTime fecha;

}