package com.repomind.backend.repository;

import com.repomind.backend.entity.CodeEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeEmbeddingRepository extends JpaRepository<CodeEmbedding, UUID> {

    @Query(value = "SELECT file_path as filePath, raw_content as rawContent, " +
                   "1 - (embedding <=> cast(:queryVector as vector)) as similarity " +
                   "FROM code_embeddings " +
                   "WHERE repo_name = :repoName " +
                   "ORDER BY embedding <=> cast(:queryVector as vector) " +
                   "LIMIT :topK", nativeQuery = true)
    List<SearchResultProjection> findSimilarCodeChunks(@Param("repoName") String repoName, 
                                                       @Param("queryVector") String queryVector, 
                                                       @Param("topK") int topK);
}
