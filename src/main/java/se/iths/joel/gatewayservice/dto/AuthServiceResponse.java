package se.iths.joel.gatewayservice.dto;

import java.util.List;

/**
 * Beskriver svaret från auth-service efter en lyckad inloggning.
 */
public record AuthServiceResponse(String accessToken, long expiresIn, String subject, List<String> roles) {
}
