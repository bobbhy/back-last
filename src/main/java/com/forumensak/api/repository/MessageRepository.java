package com.forumensak.api.repository;

import com.forumensak.api.model.social.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message,Long> {
}
