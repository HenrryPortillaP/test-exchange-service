package pe.test.exchange.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ConversionRequest(
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "El monto debe ser mayor que cero")
        BigDecimal monto,

        @NotBlank(message = "La moneda de origen es obligatoria")
        @Pattern(regexp = "[A-Za-z]{3}", message = "La moneda de origen debe tener exactamente tres letras")
        String monedaOrigen,

        @NotBlank(message = "La moneda de destino es obligatoria")
        @Pattern(regexp = "[A-Za-z]{3}", message = "La moneda de destino debe tener exactamente tres letras")
        String monedaDestino
) {
}