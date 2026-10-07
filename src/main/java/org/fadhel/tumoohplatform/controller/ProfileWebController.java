package org.fadhel.tumoohplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileWebController {

    private static final long DEFAULT_USER_ID = 1L;

    @GetMapping("/profile")
    public String profile(@RequestParam(value = "userId", required = false) Long userId, Model model) {
        model.addAttribute("userId", userId != null ? userId : DEFAULT_USER_ID);
        return "profile";
    }
}
