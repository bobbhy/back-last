package com.forumensak.api.repository;

import com.forumensak.api.model.social.Comment;
import com.forumensak.api.model.social.Like;
import com.forumensak.api.model.social.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public interface LikeRepository extends JpaRepository<Like,Long> {
    List<Like> findAllByComment(Comment comment);
    List<Like> findAllByPost(Post post);
}
