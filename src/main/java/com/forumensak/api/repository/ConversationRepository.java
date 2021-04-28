package com.forumensak.api.repository;

import com.forumensak.api.model.social.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation,Long> {
}
