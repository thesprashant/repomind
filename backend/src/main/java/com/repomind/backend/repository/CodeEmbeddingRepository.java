package com.repomind.backend.repository;

import com.repomind.backend.entity.CodeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CodeEmbeddingRepository extends JpaRepository<CodeEmbedding, UUID> {
}
