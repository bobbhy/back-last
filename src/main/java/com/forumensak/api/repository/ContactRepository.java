package com.forumensak.api.repository;

import com.forumensak.api.model.Cv;
import com.forumensak.api.model.Contact;
import com.forumensak.api.model.cv.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>{

}

