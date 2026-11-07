package es.mjsplayground.acda.mongodb.liga.model.repository;

import es.mjsplayground.acda.mongodb.liga.model.entity.Sede;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SedeRepository extends MongoRepository<Sede, String> {}
