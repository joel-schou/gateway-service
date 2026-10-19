package se.iths.joel.gatewayservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.iths.joel.gatewayservice.service.GatewayProxyService;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private final GatewayProxyService proxyService;

    public GatewayController(GatewayProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping("/products")
    public ResponseEntity<String> products(@CookieValue(name = "access_token", required = false) String accessToken) {
        return proxyService.products("Bearer " + accessToken);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<String> productById(@PathVariable Long id, HttpServletRequest request) {
        ResponseEntity<String> response = proxyService.productById(id, request.getHeader(HttpHeaders.AUTHORIZATION));

        return response;
    }


    @PostMapping("/auth/login")
    public ResponseEntity<String> login(
            @RequestBody String requestBody,
            HttpServletRequest request) {

        return (ResponseEntity<String>) proxyService.loginWithCookie(
                requestBody,
                request.getHeader(HttpHeaders.CONTENT_TYPE)
        );
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
        return proxyService.logout();
    }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(
            @RequestBody String requestBody,
            HttpServletRequest request) {

        return proxyService.createOrder(
                requestBody,
                request.getHeader(HttpHeaders.AUTHORIZATION),
                request.getHeader(HttpHeaders.CONTENT_TYPE)
        );
    }
}
