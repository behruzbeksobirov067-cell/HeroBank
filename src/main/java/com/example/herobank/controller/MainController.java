package com.example.herobank.controller;

import com.example.herobank.model.User;
import com.example.herobank.model.Card;
import com.example.herobank.model.enums.CardStatus;
import com.example.herobank.model.enums.Status;
import com.example.herobank.repository.UserRepository;
import com.example.herobank.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/main")
public class MainController {

    private final UserRepository userRepository;
    private final UserService userService;

    public MainController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public String mainPage(Model model) {
        List<User> users = userRepository.findAll();
        User user;
        if (users.isEmpty()) {
            user = new User();
            user.setPhoneNumber("+998901234567");
            user.setStatus(Status.Client);
            user = userRepository.save(user);

            userService.openCard(user.getId(), CardStatus.UzCard, new BigDecimal("2500000.00"), "1111", "123");
            userService.openCard(user.getId(), CardStatus.Visa, new BigDecimal("450.00"), "2222", "456");
            userService.openCard(user.getId(), CardStatus.Humo, new BigDecimal("120000.00"), "3333", "789");
            
            user = userRepository.findById(user.getId()).orElse(user);
        } else {
            user = users.get(0);
            if (user.getCards() == null || user.getCards().isEmpty()) {
                userService.openCard(user.getId(), CardStatus.UzCard, new BigDecimal("2500000.00"), "1111", "123");
                userService.openCard(user.getId(), CardStatus.Visa, new BigDecimal("450.00"), "2222", "456");
                user = userRepository.findById(user.getId()).orElse(user);
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("cards", user.getCards());
        return "main";
    }
}
