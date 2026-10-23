package pe.test.exchange.domain.repository;

import pe.test.exchange.domain.model.Conversion;

import java.util.List;
import java.util.Optional;

public interface ConversionRepository {

    Conversion save(Conversion conversion);

    Optional<Conversion> findById(String id);

    List<Conversion> findAll();
}