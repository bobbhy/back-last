package com.forumensak.api.model;

import com.forumensak.api.model.cv.*;
import com.forumensak.api.model.social.ProfileViewer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Cv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String image;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "about_id", referencedColumnName = "id")
    private About about;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<Experience> experiences;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<Education> educations;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<DevLanguage> devLanguages;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<NormalLanguage> normalLanguages;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<Software> softwares;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<Award> awards;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<Link> links;
    @OneToMany(orphanRemoval = true, mappedBy = "cv")
    private List<Other> others;
    @OneToMany(mappedBy = "cv", cascade = CascadeType.REMOVE)
    private List<ProfileViewer> profileViewers;
    private boolean flag;
    private boolean prive;
}
