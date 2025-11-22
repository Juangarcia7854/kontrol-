package com.tusistema.sistemaventas.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.tusistema.sistemaventas.model.Compra;

@Repository
public interface CompraRepository extends MongoRepository<Compra, String> {
}