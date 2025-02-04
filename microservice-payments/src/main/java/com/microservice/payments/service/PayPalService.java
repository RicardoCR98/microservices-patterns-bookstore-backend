// PayPalService.java
package com.microservice.payments.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PayPalService {

    private static final Logger logger = LoggerFactory.getLogger(PayPalService.class);

    @Value("${PAYPAL_CLIENT_ID}")
    private String clientId;
    @Value("${PAYPAL_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${PAYPAL_MODE}")
    private String mode;

    private final ObjectMapper mapper = new ObjectMapper();

    private String getBaseUrl() {
        return "sandbox".equalsIgnoreCase(mode)
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
    }

    @CircuitBreaker(name = "payPalServiceCircuitBreaker", fallbackMethod = "getAccessTokenFallback")
    public String getAccessToken() throws IOException, InterruptedException {
        logger.info("Fetching PayPal access token");
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v1/oauth2/token"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode json = mapper.readTree(response.body());
            logger.debug("Access token fetched successfully");
            return json.get("access_token").asText();
        } else {
            logger.error("Error fetching PayPal access token: {}", response.body());
            throw new RuntimeException("Error fetching Access Token: " + response.body());
        }
    }

    public String getAccessTokenFallback(Throwable throwable) {
        logger.error("Fallback for getAccessToken invoked: {}", throwable.getMessage());
        return null;
    }

    @CircuitBreaker(name = "payPalServiceCircuitBreaker", fallbackMethod = "captureOrderFallback")
    public CaptureResult captureOrder(String payPalOrderId) throws IOException, InterruptedException {
        logger.info("Capturing PayPal order with ID: {}", payPalOrderId);
        String accessToken = getAccessToken();
        if (accessToken == null) {
            logger.error("AccessToken is null, cannot capture PayPal order");
            throw new RuntimeException("AccessToken is null");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v2/checkout/orders/" + payPalOrderId + "/capture"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            JsonNode json = mapper.readTree(response.body());
            logger.info("Order captured successfully with ID: {}", payPalOrderId);

            String status = json.get("status").asText();
            String captureId = null;

            JsonNode purchaseUnits = json.get("purchase_units");
            if (purchaseUnits.isArray() && purchaseUnits.size() > 0) {
                JsonNode captures = purchaseUnits.get(0).get("payments").get("captures");
                if (captures.isArray() && captures.size() > 0) {
                    captureId = captures.get(0).get("id").asText();
                }
            }

            return new CaptureResult(status, captureId);
        } else {
            logger.error("Error capturing PayPal order: {}", response.body());
            throw new RuntimeException("Error capturing PayPal order: " + response.body());
        }
    }

    public CaptureResult captureOrderFallback(String payPalOrderId, Throwable throwable) {
        logger.error("Fallback for captureOrder invoked for ID: {}: {}", payPalOrderId, throwable.getMessage());
        return new CaptureResult("REJECTED", "FALLBACK_CAPTURE_ID");
    }

    public record CaptureResult(String status, String captureId) {}
}
