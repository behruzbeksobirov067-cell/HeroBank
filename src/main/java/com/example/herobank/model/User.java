package com.example.herobank.model;

import com.example.herobank.model.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
@Entity(name = "users")
public class User {

@Id
@GeneratedValue
private Long id;

private String phoneNumber;

@OneToOne
@JoinColumn(name = "passport_series")
private Passport passport;
private Status status;
@OneToMany(cascade=CascadeType.ALL,orphanRemoval = true,mappedBy = "user")
private List<Device> device;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards; // Foydalanuvchining barcha kartalari ro'yxati
}
