package com.example.herobank.model;
import com.example.herobank.model.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
@Entity(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Karta raqami (xavfsizlik uchun niqoblangan): "8600 12** **** 3456"
    @Column(name = "masked_pan", nullable = false, length = 19)
    private String maskedPan;

    // Karta raqamining xeshlangan (SHA-256) varianti (kartani qidirib topish uchun)
    @Column(name = "pan_hash", nullable = false, unique = true)
    private String panHash;

    @Column(name = "cardholder_name", nullable = false)
    private String cardholderName;

    // Karta balansi (Har doim aniqlik uchun BigDecimal va scale=2 bo'ladi!)
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    // Karta holati: ACTIVE, BLOCKED, EXPIRED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status;

    @Column(name = "pin", length = 4)
    private String pin;

    @Column(name = "cvv", length = 3)
    private String cvv;

    private boolean isBlocked;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}