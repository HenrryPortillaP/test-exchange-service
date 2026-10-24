package pe.test.exchange.application.controller;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Pattern TIPO_ERROR_JSON =
            Pattern.compile("\"error-type\"\\s*:\\s*\"([^\"]+)\"");

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(
            IllegalArgumentException ex
    ) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresDeValidacion(
            MethodArgumentNotValidException ex
    ) {
        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "La solicitud contiene campos inválidos",
                        "detalles", errores
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarCuerpoNoLegible(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error",
                        "El cuerpo debe ser JSON válido y contener una solicitud"
                ));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> manejarErrorProveedor(
            FeignException ex
    ) {
        String tipoError = extraerTipoError(ex.contentUTF8());
        if ("invalid-key".equalsIgnoreCase(tipoError)) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "error",
                            "La clave API del proveedor de tipos de cambio no es válida."
                    ));
        }
        if ("unsupported-code".equalsIgnoreCase(tipoError)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "La moneda base enviada no es compatible con el proveedor."
                    ));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error",
                        "No fue posible consultar el proveedor de tipos de cambio"
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> manejarErrorProveedorDatos(
            IllegalStateException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", ex.getMessage()));
    }

    private String extraerTipoError(String cuerpo) {
        Matcher matcher = TIPO_ERROR_JSON.matcher(cuerpo);
        return matcher.find() ? matcher.group(1) : null;
    }
}
