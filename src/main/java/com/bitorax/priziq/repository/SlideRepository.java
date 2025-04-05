package com.bitorax.priziq.repository;

import com.bitorax.priziq.domain.activity.slide.Slide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SlideRepository extends JpaRepository<Slide, String>, JpaSpecificationExecutor<Slide> {
}
