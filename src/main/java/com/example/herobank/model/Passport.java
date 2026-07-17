package com.example.herobank.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.File;
import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
@Entity(name = "passport")
public class Passport {
    @Id
    @Column(length=9)
    private String series;
    private String firstname,lastname,surname;
    @Column(name = "birth_date",nullable = false)
    private LocalDate birthdate;
    @Column(name = "issue_date",nullable = false)
    private LocalDate issue_date;
    @Column(name = "expiry_date",nullable = false)
    private LocalDate expiry_date;

    private File photo;
}
