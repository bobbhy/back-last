package com.forumensak.api.repository;

import com.forumensak.api.model.cv.OtherItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtherItemRepository extends JpaRepository<OtherItem, Long> {

}
