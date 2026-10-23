package pe.test.exchange.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.service.ConversionApplicationService;
import pe.test.exchange.domain.repository.ConversionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversiones")
public class ConversionController {

    private final ConversionApplicationService applicationService;
    private final ConversionRepository conversionRepository;

    public ConversionController(
            ConversionApplicationService applicationService,
            ConversionRepository conversionRepository
    ) {
        this.applicationService = applicationService;
        this.conversionRepository = conversionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversionResponse convertir(
            @Valid @RequestBody ConversionRequest request
    ) {
        return ConversionResponse.from(
                applicationService.convertir(
                        request.monto(),
                        request.monedaOrigen(),
                        request.monedaDestino()
                )
        );
    }

    @GetMapping
    public List<ConversionResponse> listar() {
        return conversionRepository.findAll()
                .stream()
                .map(ConversionResponse::from)
                .toList();
    }
}
