package com.forumensak.api.model;

import com.forumensak.api.model.company.AboutCompany;
import com.forumensak.api.model.cv.Link;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String companyImage;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "about_id", referencedColumnName = "id")
    private AboutCompany aboutCompany;
    @OneToMany(orphanRemoval=true,mappedBy="company")
    private List<Link> links;
    private boolean flag;
}
