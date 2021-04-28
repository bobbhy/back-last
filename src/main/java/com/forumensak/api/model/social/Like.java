package com.forumensak.api.model.social;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long likerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Comment comment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Post post;
}
