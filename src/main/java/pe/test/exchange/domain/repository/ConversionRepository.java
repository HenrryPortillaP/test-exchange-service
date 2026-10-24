package pe.test.exchange.domain.repository;

import pe.test.exchange.domain.model.Conversion;

import java.util.List;

public interface ConversionRepository {

    Conversion save(Conversion conversion);

    List<Conversion> findAll();
}