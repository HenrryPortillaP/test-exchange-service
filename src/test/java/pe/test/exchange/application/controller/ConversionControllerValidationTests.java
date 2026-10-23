package pe.test.exchange.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.test.exchange.application.service.ConversionApplicationService;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Moneda;
import pe.test.exchange.domain.repository.ConversionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversionController.class)
@Import(ApiExceptionHandler.class)
class ConversionControllerValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConversionApplicationService applicationService;

    @MockitoBean
    private ConversionRepository conversionRepository;

    @Test
    void rejectsMissingAmount() throws Exception {
        assertBadRequest("""
                {"monedaOrigen":"USD","monedaDestino":"PEN"}
                """);
    }

    @Test
    void rejectsZeroAmount() throws Exception {
        assertBadRequest("""
                {"monto":0,"monedaOrigen":"USD","monedaDestino":"PEN"}
                """);
    }

    @Test
    void rejectsNegativeAmount() throws Exception {
        assertBadRequest("""
                {"monto":-1,"monedaOrigen":"USD","monedaDestino":"PEN"}
                """);
    }

    @Test
    void rejectsMissingCurrencyCodes() throws Exception {
        assertBadRequest("""
                {"monto":10}
                """);
    }

    @Test
    void rejectsCurrencyCodesWithInvalidFormat() throws Exception {
        assertBadRequest("""
                {"monto":10,"monedaOrigen":"US","monedaDestino":"P3N"}
                """);
    }

    @Test
    void rejectsEmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/conversiones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(
                        "El cuerpo debe ser JSON válido y contener una solicitud"
                ));

        verifyNoInteractions(applicationService);
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/conversiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(
                        "El cuerpo debe ser JSON válido y contener una solicitud"
                ));

        verifyNoInteractions(applicationService);
    }

    @Test
    void rejectsJsonNullBody() throws Exception {
        mockMvc.perform(post("/api/v1/conversiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(
                        "El cuerpo debe ser JSON válido y contener una solicitud"
                ));

        verifyNoInteractions(applicationService);
    }

    @Test
    void acceptsValidRequestAndReturnsCreated() throws Exception {
        Conversion conversion = new Conversion(
                "conversion-id",
                new Moneda("USD"),
                new Moneda("PEN"),
                new BigDecimal("10"),
                new BigDecimal("3.5"),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        when(applicationService.convertir(
                any(BigDecimal.class),
                eq("USD"),
                eq("PEN")
        )).thenReturn(conversion);

        mockMvc.perform(post("/api/v1/conversiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto":10,"monedaOrigen":"USD","monedaDestino":"PEN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("conversion-id"))
                .andExpect(jsonPath("$.montoConvertido").value(35.0));

        verify(applicationService).convertir(
                new BigDecimal("10"),
                "USD",
                "PEN"
        );
        verifyNoInteractions(conversionRepository);
    }

    private void assertBadRequest(String requestBody) throws Exception {
        mockMvc.perform(post("/api/v1/conversiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(
                        "La solicitud contiene campos inválidos"
                ))
                .andExpect(jsonPath("$.detalles").isArray());

        verifyNoInteractions(applicationService);
    }
}
