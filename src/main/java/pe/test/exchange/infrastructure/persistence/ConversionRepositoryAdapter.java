package pe.test.exchange.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.test.exchange.domain.model.Conversion;
import pe.test.exchange.domain.model.Moneda;
import pe.test.exchange.domain.repository.ConversionRepository;

import java.util.List;

@Repository
public class ConversionRepositoryAdapter
        implements ConversionRepository {

    private final SpringDataConversionRepository jpaRepository;

    public ConversionRepositoryAdapter(
            SpringDataConversionRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conversion save(Conversion conversion) {
        ConversionEntity entity = new ConversionEntity(
                conversion.getId(),
                conversion.getMonedaOrigen().codigo(),
                conversion.getMonedaDestino().codigo(),
                conversion.getMonto(),
                conversion.getTipoCambio(),
                conversion.getMontoConvertido(),
                conversion.getFecha()
        );

        ConversionEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Conversion> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Conversion toDomain(ConversionEntity entity) {
        return new Conversion(
                entity.getId(),
                new Moneda(entity.getMonedaOrigen()),
                new Moneda(entity.getMonedaDestino()),
                entity.getMonto(),
                entity.getTipoCambio(),
                entity.getMontoConvertido(),
                entity.getFecha()
        );
    }
}