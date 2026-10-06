package org.fadhel.tumoohplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"id"}, allowGetters = true, ignoreUnknown = true)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotEmpty(message = "English name is required")
    @Size(max = 100, message = "English name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    private String nameEn;

    @Size(max = 100, message = "Arabic name must be at most 100 characters")
    @Column( length = 100)
    private String nameAr;

    @ElementCollection
    @CollectionTable(name = "company_alias", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "alias", length = 100)
    private List<String> aliases = new ArrayList<>();

    @NotEmpty(message = "English industry is required")
    @Size(max = 60, message = "English industry must be at most 60 characters")
    @Column( length = 60)
    private String industryEn;

    @Size(max = 60, message = "Arabic industry must be at most 60 characters")
    @Column( length = 60)
    private String industryAr;

    @Pattern(regexp = "^https?://[\\w.-]+\\.[a-zA-Z]{2,}(/\\S*)?$", message = "Website must be a valid URL starting with http:// or https://")
    @Column(unique = true, length = 150)
    private String website;

    @Size(max = 500, message = "Logo URL must be at most 500 characters")
    @Column(length = 500)
    private String companyLogoUrl;

    @Size(max = 500, message = "English description must be at most 500 characters")
    @Column( length = 500)
    private String descriptionEn;

    @Size(max = 500, message = "Arabic description must be at most 500 characters")
    @Column( length = 500)
    private String descriptionAr;

    @Column(nullable = false)
    private Boolean verified = false;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Job> jobs;


}