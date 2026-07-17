package com.example.herobank.controller.card;

import com.example.herobank.model.Card;
import com.example.herobank.model.enums.CardStatus;
import com.example.herobank.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/api/cards")
public class AddCardController {

    private final UserService userService;

    public AddCardController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create/Open a new card for a user.
     */
    @PostMapping
    public ResponseEntity<Card> createCard(
            @RequestParam Long userId,
            @RequestParam CardStatus status,
            @RequestParam BigDecimal initialDeposit,
            @RequestParam(required = false) String pin,
            @RequestParam(required = false) String cvv) {
        Card card = userService.openCard(userId, status, initialDeposit, pin, cvv);
        return ResponseEntity.ok(card);
    }

    /**
     * Retrieve a specific card by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Card> getCard(@PathVariable Long id) {
        Card card = userService.getCardDetails(id);
        return ResponseEntity.ok(card);
    }

    /**
     * Retrieve all cards belonging to a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Card>> getUserCards(@PathVariable Long userId) {
        List<Card> cards = userService.getUserCards(userId);
        return ResponseEntity.ok(cards);
    }

    /**
     * Update card details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Card> updateCard(@PathVariable Long id, @RequestBody Card card) {
        Card updated = userService.updateCard(id, card);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete/Remove a specific card.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        userService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfer money between cards.
     */
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(
            @RequestParam String fromPanHash,
            @RequestParam String toPan,
            @RequestParam BigDecimal amount) {
        userService.transferMoneyByPan(fromPanHash, toPan, amount);
        return ResponseEntity.ok().build();
    }
}
