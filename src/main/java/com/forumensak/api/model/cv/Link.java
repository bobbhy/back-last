package com.forumensak.api.model.cv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.forumensak.api.model.Company;
import com.forumensak.api.model.Cv;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String url;
    @ManyToOne(fetch=FetchType.LAZY)
    @JsonIgnore
    private Cv cv;
    @ManyToOne(fetch=FetchType.LAZY)
    @JsonIgnore
    private Company company;
}
