package com.isi4.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "administrateurs")
@Data
@EqualsAndHashCode(callSuper = true)
public class Administrateur extends Utilisateur {
    
    // Méthodes métier spécifiques à l'administrateur
    // Ces méthodes seront implémentées dans le service
}
