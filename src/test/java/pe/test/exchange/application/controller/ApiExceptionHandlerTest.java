package pe.test.exchange.application.controller;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void reportsInvalidApiKeyFromFeignErrorBody() {
        FeignException exception = providerError(
                """
                        {"result":"error","error-type":"invalid-key"}
                        """
        );

        ResponseEntity<Map<String, String>> response =
                handler.manejarErrorProveedor(exception);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals(
                "La clave API del proveedor de tipos de cambio no es válida.",
                response.getBody().get("error")
        );
    }

    @Test
    void reportsUnsupportedBaseCurrencyFromFeignErrorBody() {
        FeignException exception = providerError(
                """
                        {"result":"error","error-type":"unsupported-code"}
                        """
        );

        ResponseEntity<Map<String, String>> response =
                handler.manejarErrorProveedor(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "La moneda base enviada no es compatible con el proveedor.",
                response.getBody().get("error")
        );
    }

    private FeignException providerError(String body) {
        FeignException exception = mock(FeignException.class);
        when(exception.contentUTF8()).thenReturn(body);
        return exception;
    }
}
