package se.iths.joel.gatewayservice.dto;

import java.util.List;

public record LoginResponse(long expiresIn, String subject, List<String> roles) {
}
