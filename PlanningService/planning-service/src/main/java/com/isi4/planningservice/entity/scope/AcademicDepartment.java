package com.isi4.planningservice.entity.scope;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_departments")
public class AcademicDepartment {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "school_id", nullable = false)
    private String schoolId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public AcademicDepartment() {
    }

    public AcademicDepartment(String id, String schoolId, String code, String libelle, boolean active) {
        this.id = id;
        this.schoolId = schoolId;
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

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
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
