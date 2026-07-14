package com.chandravijay.banking.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Only relevant when the frontend build (frontend/dist) has been copied into
 * src/main/resources/static and is served from this same Spring Boot app
 * (Deployment Option A). Without this, refreshing on a client-side route like
 * /dashboard would 404 because no such static file exists.
 */
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
