package com.isi4.planningservice.entity.scope;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_groups")
public class AcademicGroup {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "level_id", nullable = false)
    private String levelId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public AcademicGroup() {
    }

    public AcademicGroup(String id, String levelId, String code, String libelle, Integer capacity, boolean active) {
        this.id = id;
        this.levelId = levelId;
        this.code = code;
        this.libelle = libelle;
        this.capacity = capacity;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
