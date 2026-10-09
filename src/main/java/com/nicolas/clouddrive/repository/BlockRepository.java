package com.nicolas.clouddrive.repository;

import com.nicolas.clouddrive.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, UUID> {

    Optional<Block> findByHash(String hash);
}
