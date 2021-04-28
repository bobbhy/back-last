package com.forumensak.api.repository;

import com.forumensak.api.model.Etablishment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablishementRepository extends JpaRepository<Etablishment,Long> {
    Etablishment getById(Long id);
}
