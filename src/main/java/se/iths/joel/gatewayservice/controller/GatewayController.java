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
    public ResponseEntity<String> productById(
            @PathVariable Long id,
            @CookieValue(name = "access_token", required = false) String access_Token) {

        return proxyService.productById(id, access_Token);
    }

    @GetMapping("/auth/me")
    public ResponseEntity<String> session(@CookieValue(name = "access_token", required = false) String accessToken) {
        return proxyService.session(accessToken);
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
        @CookieValue(name = "access_token", required = false) String accessToken,
                HttpServletRequest request) {


        return proxyService.createOrder(
                requestBody,
                "Bearer " + accessToken,
                request.getHeader(HttpHeaders.CONTENT_TYPE)
        );
    }

    @PostMapping("/add-products")
    public ResponseEntity<String> addProduct(
            @RequestBody String requestBody,
            @CookieValue(name = "access_token", required = false)
            String accessToken,
            HttpServletRequest request) {

        return proxyService.addProduct(
                requestBody,
                "Bearer " + accessToken,
                request.getHeader(HttpHeaders.CONTENT_TYPE)
        );

    }



}
