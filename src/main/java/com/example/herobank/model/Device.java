package com.example.herobank.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceModel;
    private String deviceToken;
    @ManyToOne
    @JoinColumn(name = "user_id") // Baza jadvalidagi user_id ustuni
    private User user;
}
