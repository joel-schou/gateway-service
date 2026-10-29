package se.iths.joel.gatewayservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.iths.joel.gatewayservice.dto.AuthServiceResponse;
import se.iths.joel.gatewayservice.dto.LoginResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


/**
 * Vidarebefordrar anrop till bakomliggande tjänster och hanterar inloggningscookien.
 */
@Service
public class GatewayProxyService {

    private final String authServerUrl;
    private final String productServiceUrl;
    private final String orderServiceUrl;
    private final String paymentServiceUrl;
    private final HttpClient httpClient;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GatewayProxyService(
            @Value("${app.auth-server-url}") String authServerUrl,
            @Value("${app.product-service-url}") String productServiceUrl,
            @Value("${app.order-service-url}") String orderServiceUrl,
            @Value("{app.payment-service-url}") String paymentServiceUrl,
            @Value("${COOKIE_SAME_SITE:Lax}") String cookieSameSite,
            @Value("${COOKIE_SECURE:false}") boolean cookieSecure,
            HttpClient httpClient) {
        this.authServerUrl = authServerUrl;
        this.productServiceUrl = productServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
        this.paymentServiceUrl = paymentServiceUrl;
        this.httpClient = httpClient;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    // hjälpmetoder

    public ResponseEntity<String> products(String authorization) {
        ResponseEntity<String> getResponse = forwardGet(productServiceUrl, "/products", authorization);
        return getResponse;

    }

    private ResponseEntity<String> forwardGet(
            String baseUrl, String path, String authorization) {

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET();

        ResponseEntity<String> sendResponse = send(builder, authorization);
        return sendResponse;

    }

    private ResponseEntity<String> forwardPost(
            String baseUrl,
            String path,
            String requestBody,
            String authorization,
            String contentType) {

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (contentType != null && !contentType.isBlank()) {
            builder.header(HttpHeaders.CONTENT_TYPE, contentType);

        }
        return send(builder, authorization);
    }

    private ResponseEntity<String> forwardDelete(
            String baseUrl,
            String path,
            String authorization) {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(URI.create(baseUrl + path))
                        .DELETE();

        return send(builder, authorization);
    }

    private ResponseEntity<String> forwardPut(
            String baseUrl,
            String path,
            String requestBody,
            String authorization,
            String contentType) {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(URI.create(baseUrl + path))
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody));

        if (contentType != null && !contentType.isBlank()) {
            builder.header(HttpHeaders.CONTENT_TYPE, contentType);
        }

        return send(builder, authorization);
    }


    // crud metoder
    public ResponseEntity<String> productById(Long id, String accessToken) {
    return forwardGet(productServiceUrl, "/products/" + id, "Bearer " + accessToken);


    }

    public ResponseEntity<String> login(String requestBody, String contentType) {
        return forwardPost(
                authServerUrl,
                "/auth/login",
                requestBody,
                null,
                contentType
        );

    }

    public ResponseEntity<String> createOrder(
            String requestBody,
            String authorization,
            String contentType
    ) {

        return forwardPost(
                orderServiceUrl,
                "/orders",
                requestBody,
                authorization,
                contentType
        );
    }

    private ResponseEntity<String> send(HttpRequest.Builder builder, String authorization) {
        if (authorization != null && !authorization.isBlank()) {
            builder.header(
                    HttpHeaders.AUTHORIZATION,
                    authorization
            );
        }
        try {
            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );


            ResponseEntity.BodyBuilder responseBuilder =
                    ResponseEntity.status(response.statusCode());

            response.headers()
                    .firstValue(HttpHeaders.CONTENT_TYPE)
                    .ifPresent(contentType -> responseBuilder.header(
                            HttpHeaders.CONTENT_TYPE,
                            contentType
                    ));

            return responseBuilder.body(response.body());

        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Kunde inte nå bakomliggande tjänst");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Anropet avbröts");
        }

    }

    public ResponseEntity<?> loginWithCookie(String requestBody, String contentType) {
        HttpRequest.Builder request = HttpRequest.newBuilder(
                URI.create(authServerUrl + "/auth/login")
        ).header(HttpHeaders.CONTENT_TYPE, contentType).POST(
                HttpRequest.BodyPublishers.ofString(requestBody)
        );
        return sendLogin(request);
    }

    private ResponseEntity<?> sendLogin(HttpRequest.Builder request) {
        try {
            HttpResponse<String> response = httpClient.send(
                    request.build(), HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return ResponseEntity.status(response.statusCode()).body(response.body());
            }
            return createLoginResponse(response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return ResponseEntity.status(502)
                    .body("Gateway could not reach auth service");
        }
    }

    private ResponseEntity<?> createLoginResponse(String responseBody) {
        try {
            AuthServiceResponse authServiceResponse = objectMapper.readValue(responseBody, AuthServiceResponse.class);
            ResponseCookie cookie = ResponseCookie.from("access_token", authServiceResponse.accessToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .sameSite(cookieSameSite)
                    .path("/")
                    .maxAge(Duration.ofSeconds(authServiceResponse.expiresIn())).build();

            LoginResponse loginResponse = new LoginResponse(
                    authServiceResponse.expiresIn(),
                    authServiceResponse.subject(),
                    authServiceResponse.roles()
            );

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(loginResponse);
        } catch (JacksonException ex) {
            return ResponseEntity.status(502).body("Auth service returned invalid response.");

        }
    }

    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    public ResponseEntity<String> session(String accessToken) {
        return forwardGet(authServerUrl, "/auth/me", "Bearer " + accessToken);
    }

    public ResponseEntity<String> addProduct(String body, String authHeader, String contentType) {
        return forwardPost(productServiceUrl, "/products", body, authHeader, contentType);

    }

    public ResponseEntity<String> createPaymentIntent(
            String requestBody,
            String authorization,
            String contentType
    ) {
        return forwardPost(
                paymentServiceUrl,
                "/api/payments/create-intent",
                 requestBody,
                authorization,
                contentType
        );
    }
}








