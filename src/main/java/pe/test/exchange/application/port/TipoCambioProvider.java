package pe.test.exchange.application.port;

import java.math.BigDecimal;

public interface TipoCambioProvider {

    BigDecimal obtenerTipoCambio(String monedaOrigen, String monedaDestino);
}
