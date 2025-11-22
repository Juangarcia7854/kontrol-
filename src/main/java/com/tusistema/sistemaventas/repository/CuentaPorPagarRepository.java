package com.tusistema.sistemaventas.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.tusistema.sistemaventas.model.CuentaPorPagar;

@Repository
public interface CuentaPorPagarRepository extends MongoRepository<CuentaPorPagar, String> {
}