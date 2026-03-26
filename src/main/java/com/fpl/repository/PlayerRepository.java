package com.fpl.repository;

import com.fpl.model.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findFirstByNameIgnoreCaseOrderByIdAsc(String name);
}