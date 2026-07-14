package com.chandravijay.banking.controller;

import com.chandravijay.banking.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @GetMapping("/api/accounts/{id}/statement")
    public ResponseEntity<byte[]> downloadStatement(Authentication auth, @PathVariable Long id) {
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        byte[] pdf = statementService.generateStatementPdf(id, auth.getName(), isAdmin);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("statement-account-" + id + ".pdf")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
