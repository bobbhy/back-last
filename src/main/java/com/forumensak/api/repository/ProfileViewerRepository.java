package com.forumensak.api.repository;


import com.forumensak.api.model.Cv;
import com.forumensak.api.model.social.ProfileViewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public interface ProfileViewerRepository extends JpaRepository<ProfileViewer,Long> {
    List<ProfileViewer> findAllByCv(Cv cv);
}
