package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.slide.SlideElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SlideElementRepository extends JpaRepository<SlideElement, String>, JpaSpecificationExecutor<SlideElement> {
}
