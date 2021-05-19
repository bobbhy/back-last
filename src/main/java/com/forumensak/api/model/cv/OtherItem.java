package com.forumensak.api.model.cv;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.dynamic.TypeResolutionStrategy.Lazy;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private int value;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Other other;

}
