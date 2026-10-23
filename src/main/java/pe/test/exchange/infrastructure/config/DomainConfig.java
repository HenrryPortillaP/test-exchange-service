package pe.test.exchange.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.test.exchange.domain.service.TipoCambioDomainService;

@Configuration
public class DomainConfig {

    @Bean
    public TipoCambioDomainService tipoCambioDomainService() {
        return new TipoCambioDomainService();
    }
}