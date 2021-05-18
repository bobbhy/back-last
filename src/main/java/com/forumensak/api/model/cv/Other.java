package com.forumensak.api.model.cv;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.forumensak.api.model.Cv;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class Other {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private int value;
    @OneToMany(orphanRemoval = true, mappedBy = "other")
    private List<OtherItem> otherItems;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Cv cv;

}
