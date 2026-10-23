package pe.test.exchange.domain.exception;

public class MonedaNoSoportadaException extends RuntimeException {

    public MonedaNoSoportadaException(String codigo) {
        super("Moneda no soportada: " + codigo);
    }
}
