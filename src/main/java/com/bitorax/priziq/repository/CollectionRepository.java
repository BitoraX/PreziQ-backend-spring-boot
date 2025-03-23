package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CollectionRepository extends JpaRepository<Collection, String>, JpaSpecificationExecutor<Collection> {
}
