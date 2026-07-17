package com.example.herobank.controller.card;

import com.example.herobank.model.Card;
import com.example.herobank.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/cards/{cardId}/settings")
public class CardSettingsController {

    private final UserService userService;

    public CardSettingsController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Change the card's PIN code.
     */
    @PutMapping("/pin")
    public ResponseEntity<Card> changePin(
            @PathVariable Long cardId,
            @RequestParam String oldPin,
            @RequestParam String newPin) {
        Card card = userService.changeCardPin(cardId, oldPin, newPin);
        return ResponseEntity.ok(card);
    }

    /**
     * Get the full requisites of the card.
     */
    @GetMapping("/requisites")
    public ResponseEntity<Card> getRequisites(@PathVariable Long cardId) {
        Card card = userService.getCardDetails(cardId);
        return ResponseEntity.ok(card);
    }
}
