package com.forumensak.api.controller;

import com.forumensak.api.model.Contact;
import com.forumensak.api.model.social.Comment;
import com.forumensak.api.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contact/")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;
    @GetMapping("/messages")
    public ResponseEntity<?> getall()
    {
        return ResponseEntity.status(HttpStatus.OK).body(contactRepository.findAll());
    }
    @DeleteMapping("/message/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable long id)
    {
        contactRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }
    @PostMapping("/message")
    public ResponseEntity<?> ajoutcontact(@RequestBody Contact Contact) {
        contactRepository.save(Contact);
        return ResponseEntity.status(HttpStatus.OK).body(Contact);

    }
}
