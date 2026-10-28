package com.isi4.planningservice.entity.scope;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_specialities")
public class AcademicSpeciality {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "department_id", nullable = false)
    private String departmentId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public AcademicSpeciality() {
    }

    public AcademicSpeciality(String id, String departmentId, String code, String libelle, boolean active) {
        this.id = id;
        this.departmentId = departmentId;
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

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
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
