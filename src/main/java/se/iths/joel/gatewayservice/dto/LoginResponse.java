package se.iths.joel.gatewayservice.dto;

import java.util.List;

/**
 * Beskriver inloggningssvaret som gatewayn skickar till frontend.
 */
public record LoginResponse(long expiresIn, String subject, List<String> roles) {
}
