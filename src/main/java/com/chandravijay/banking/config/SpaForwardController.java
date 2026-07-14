package com.chandravijay.banking.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SpaForwardController {

    @GetMapping({
            "/dashboard",
            "/transfer",
            "/admin",
            "/login",
            "/register",
            "/accounts/{id}"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
