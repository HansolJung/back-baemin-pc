package it.korea.app_bmpc.email.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.korea.app_bmpc.email.service.EmailService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/test/email")
    public void sendMimeMessage() {
        emailService.sendMimeMessage();
    }
}
