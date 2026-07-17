package com.example.herobank.service;

import com.example.herobank.exception.BankException;
import com.example.herobank.model.Card;
import com.example.herobank.model.User;
import com.example.herobank.model.enums.CardStatus;
import com.example.herobank.repository.CardRepository;
import com.example.herobank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final Random random = new Random();

    public UserService(UserRepository userRepository, CardRepository cardRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    /**
     * Opens a new card for a specific user.
     */
    @Transactional
    public Card openCard(Long userId, CardStatus status, BigDecimal initialDeposit) {
        return openCard(userId, status, initialDeposit, null, null);
    }

    @Transactional
    public Card openCard(Long userId, CardStatus status, BigDecimal initialDeposit, String pin, String cvv) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankException("User not found with ID: " + userId));

        if (initialDeposit == null || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankException("Initial deposit cannot be negative");
        }

        // Generate card number details
        String rawPan = generateRawPan(status);
        String maskedPan = maskPan(rawPan);
        String panHash = hashPan(rawPan);

        // Fetch cardholder name from User passport details if available
        String cardholderName = "HEROBANK CUSTOMER";
        if (user.getPassport() != null) {
            String first = user.getPassport().getFirstname();
            String last = user.getPassport().getLastname();
            if (first != null && !first.trim().isEmpty() && last != null && !last.trim().isEmpty()) {
                cardholderName = (first.trim() + " " + last.trim()).toUpperCase();
            }
        }

        Card card = new Card();
        card.setMaskedPan(maskedPan);
        card.setPanHash(panHash);
        card.setCardholderName(cardholderName);
        card.setBalance(initialDeposit);
        card.setExpiryDate(LocalDate.now().plusYears(5)); // Cards valid for 5 years
        card.setStatus(status);
        card.setBlocked(false);
        card.setUser(user);

        if (pin == null || pin.trim().isEmpty()) {
            pin = generateRandomPin();
        }
        if (cvv == null || cvv.trim().isEmpty()) {
            cvv = generateRandomCvv();
        }
        card.setPin(pin);
        card.setCvv(cvv);

        return cardRepository.save(card);
    }

    @Transactional
    public void transferMoneyByPan(String fromPanHash, String toPan, BigDecimal amount) {
        String cleanPan = toPan.replace(" ", "").replace("-", "");
        String toPanHash = hashPan(cleanPan);
        transferMoney(fromPanHash, toPanHash, amount);
    }

    /**
     * Safely transfers money between two cards.
     */
    @Transactional
    public void transferMoney(String fromPanHash, String toPanHash, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Transfer amount must be greater than zero");
        }

        Card fromCard = cardRepository.findByPanHash(fromPanHash)
                .orElseThrow(() -> new BankException("Source card not found"));

        Card toCard = cardRepository.findByPanHash(toPanHash)
                .orElseThrow(() -> new BankException("Destination card not found"));

        if (fromCard.isBlocked()) {
            throw new BankException("Source card is blocked and cannot make payments");
        }

        if (toCard.isBlocked()) {
            throw new BankException("Destination card is blocked and cannot receive payments");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new BankException("Insufficient funds on source card");
        }

        // Update balances
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    /**
     * Fetches details of a specific card.
     */
    public Card getCardDetails(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Card not found with ID: " + cardId));
    }

    /**
     * Blocks or unblocks a specific card.
     */
    @Transactional
    public Card blockCard(Long cardId, boolean block) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Card not found with ID: " + cardId));

        card.setBlocked(block);
        return cardRepository.save(card);
    }

    // Helper to generate raw PAN digits
    private String generateRawPan(CardStatus status) {
        String prefix;
        switch (status) {
            case UzCard:
                prefix = "8600";
                break;
            case Humo:
                prefix = "9860";
                break;
            case Visa:
                prefix = "4000";
                break;
            case MasterCard:
                prefix = "5100";
                break;
            case VisaGold:
                prefix = "4111";
                break;
            default:
                prefix = "9999";
        }
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // Helper to mask PAN to 19 characters format: "8600 12** **** 3456"
    private String maskPan(String pan) {
        if (pan == null || pan.length() < 16) {
            return "XXXX XXXX XXXX XXXX";
        }
        return pan.substring(0, 4) + " " +
               pan.substring(4, 6) + "** **** " +
               pan.substring(12, 16);
    }

    // Helper to calculate SHA-256 hash of raw PAN
    private String hashPan(String pan) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pan.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new BankException("Failed to generate secure PAN hash signature");
        }
    }

    @Transactional
    public Card updateCard(Long cardId, Card updatedCard) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Card not found with ID: " + cardId));
        
        if (updatedCard.getBalance() != null) {
            card.setBalance(updatedCard.getBalance());
        }
        if (updatedCard.getStatus() != null) {
            card.setStatus(updatedCard.getStatus());
        }
        if (updatedCard.getCardholderName() != null) {
            card.setCardholderName(updatedCard.getCardholderName());
        }
        if (updatedCard.getPin() != null && !updatedCard.getPin().trim().isEmpty()) {
            card.setPin(updatedCard.getPin());
        }
        if (updatedCard.getCvv() != null && !updatedCard.getCvv().trim().isEmpty()) {
            card.setCvv(updatedCard.getCvv());
        }
        card.setBlocked(updatedCard.isBlocked());
        return cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Card not found with ID: " + cardId));
        cardRepository.delete(card);
    }

    @Transactional
    public Card changeCardPin(Long cardId, String oldPin, String newPin) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BankException("Card not found with ID: " + cardId));
        if (oldPin == null || !oldPin.equals(card.getPin())) {
            throw new BankException("Incorrect old PIN");
        }
        if (newPin == null || newPin.length() != 4 || !newPin.matches("\\d{4}")) {
            throw new BankException("New PIN must be exactly 4 digits");
        }
        card.setPin(newPin);
        return cardRepository.save(card);
    }

    public java.util.List<Card> getUserCards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankException("User not found with ID: " + userId));
        return user.getCards();
    }

    private String generateRandomPin() {
        return String.format("%04d", random.nextInt(10000));
    }

    private String generateRandomCvv() {
        return String.format("%03d", random.nextInt(1000));
    }
}
