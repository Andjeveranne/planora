package com.isi4.planningservice.entity.scope;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_levels")
public class AcademicLevel {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "speciality_id", nullable = false)
    private String specialityId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public AcademicLevel() {
    }

    public AcademicLevel(String id, String specialityId, String code, String libelle, boolean active) {
        this.id = id;
        this.specialityId = specialityId;
        this.code = code;
        this.libelle = libelle;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpecialityId() {
        return specialityId;
    }

    public void setSpecialityId(String specialityId) {
        this.specialityId = specialityId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
