package com.forumensak.api.repository;

import com.forumensak.api.model.cv.Other;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtherRepository extends JpaRepository<Other, Long> {
}
