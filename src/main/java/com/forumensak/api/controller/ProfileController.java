package com.forumensak.api.controller;

import com.forumensak.api.payload.MessagePayload;
import com.forumensak.api.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired
    ProfileService profileService;

    @GetMapping("")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String autHeader) {
        return profileService.getProfile(autHeader);
    }

    @GetMapping("/{var}")
    public ResponseEntity<?> searchUsers(@PathVariable String var) {
        return profileService.searchUsers(var);
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAll() {
        return profileService.getAll();
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<?> connectTo(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return profileService.connectTo(authHeader, id);
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<?> accept(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return profileService.accept(authHeader, id);
    }

    @PostMapping("/notifications")
    public ResponseEntity<?> handleNotifications(@RequestHeader("Authorization") String authHeader) {
        return profileService.handleNotifications(authHeader);
    }

    @PostMapping("/notification/{id}")
    public ResponseEntity<?> handleNotification(@PathVariable long id) {
        return profileService.handleNotification(id);
    }

    @DeleteMapping("/disconnect/{id}")
    public ResponseEntity<?> disconnect(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return profileService.disconnect(authHeader, id);
    }

    @GetMapping("/etablishment/all")
    public ResponseEntity<?> getAllEtablishment() {
        return profileService.getAllEtablishment();
    }

    @DeleteMapping("/notification/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        return profileService.deleteNotification(id);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {
        return profileService.getAll(authHeader);
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<?> SendMessage(@RequestHeader("Authorization") String authHeader, @RequestBody MessagePayload messagePayload) {
        return profileService.sendMessage(authHeader, messagePayload);
    }

    @GetMapping("/allNotifications")
    public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String authHeader) {
        return profileService.getNotifications(authHeader);
    }

    @GetMapping("/allMessages/{id}")
    public ResponseEntity<?> getMessages(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return profileService.getMessages(authHeader, id);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        return profileService.deleteMessage(id);
    }

    @GetMapping("/reported")
    public ResponseEntity<?> getReported() {
        return profileService.getReported();
    }

    @GetMapping("/isreported")
    public ResponseEntity<?> getIsReported(@RequestHeader("Authorization") String authHeader) {
        return profileService.getIsReported(authHeader);
    }
}
