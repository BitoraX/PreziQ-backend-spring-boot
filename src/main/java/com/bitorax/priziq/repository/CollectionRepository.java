package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.Collection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, String>, JpaSpecificationExecutor<Collection> {
    @Query("SELECT c.topic AS topic, c AS collection FROM Collection c " + "WHERE c.isPublished = true " + "ORDER BY c.createdAt DESC")
    List<Object[]> findPublishedGroupedByTopic(Pageable pageable);;
}
