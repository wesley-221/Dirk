package com.beneluwux.repositories;

import com.beneluwux.models.entities.Birthday;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BirthdayRepository extends CrudRepository<Birthday, Integer> {
}
