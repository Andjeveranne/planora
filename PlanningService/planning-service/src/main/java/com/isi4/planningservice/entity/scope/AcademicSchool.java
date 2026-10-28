package com.isi4.planningservice.entity.scope;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_schools")
public class AcademicSchool {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public AcademicSchool() {
    }

    public AcademicSchool(String id, String code, String nom, boolean active) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
