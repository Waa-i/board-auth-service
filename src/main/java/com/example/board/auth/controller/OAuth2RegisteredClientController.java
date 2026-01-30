package com.example.board.auth.controller;

import com.example.board.auth.dto.command.OAuth2RegisteredClientCommand;
import com.example.board.auth.dto.request.OAuth2RegisteredClientRequest;
import com.example.board.auth.service.impl.OAuth2RegisteredClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuth2RegisteredClientController {
    private final OAuth2RegisteredClientService oAuth2RegisteredClientService;

    @GetMapping("/oauth2-client/register")
    public String registerForm(Model model) {
        model.addAttribute("form", OAuth2RegisteredClientRequest.empty());
        return "auth/oauth2-client/register";
    }

    @PostMapping("/oauth2-client/register")
    public String register(@Valid @ModelAttribute("form") OAuth2RegisteredClientRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/oauth2-client/register";
        }
        oAuth2RegisteredClientService.register(new OAuth2RegisteredClientCommand(request.clientName(), request.clientSecret(), request.redirectUris()));
        return "redirect:/auth/oauth2-client/list";
    }

    @GetMapping("/oauth2-client/list")
    public String getClients(Model model) {
        var clients = oAuth2RegisteredClientService.getClients();
        model.addAttribute("clients", clients);
        return "auth/oauth2-client/list";
    }
}
