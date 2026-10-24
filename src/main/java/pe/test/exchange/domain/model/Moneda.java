package pe.test.exchange.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Moneda(String codigo) {

    private static final Pattern FORMATO_CODIGO =
            Pattern.compile("[A-ZÑ]{3}");

    public Moneda {
        Objects.requireNonNull(
                codigo,
                "El código de moneda es obligatorio"
        );

        codigo = codigo.trim().toUpperCase(Locale.ROOT);

        if (!FORMATO_CODIGO.matcher(codigo).matches()) {
            throw new IllegalArgumentException(
                    "El código de moneda debe tener tres letras"
            );
        }
    }
}
