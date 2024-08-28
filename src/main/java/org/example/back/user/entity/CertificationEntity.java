package org.example.back.user.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "certification")
@Table(name = "certification")
public class CertificationEntity {
    
    @Id
    private String email;

    @Column
    private String username;

    @Column
    private String certificationNumber;
}
