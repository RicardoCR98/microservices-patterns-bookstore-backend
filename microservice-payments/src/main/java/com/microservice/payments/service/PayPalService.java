package com.microservice.payments.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

    @Value("${paypal.client-id}")
    private String clientId;
    @Value("${paypal.client-secret}")
    private String clientSecret;
    @Value("${paypal.mode}")
    private String mode; // "sandbox" o "live"


    private final ObjectMapper mapper = new ObjectMapper();

    private String getBaseUrl() {
        return "sandbox".equalsIgnoreCase(mode)
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
    }

    /**
     * Obtiene accessToken llamando a /v1/oauth2/token
     */
    public String getAccessToken() throws IOException, InterruptedException {
        System.out.println("El CLIENT_ID es: " + clientId);
        System.out.println("El CLIENT_SECRET es: " + clientSecret);

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
            return json.get("access_token").asText();
        } else {
            throw new RuntimeException("Error al obtener Access Token de PayPal: " + response.body());
        }
    }

    /**
     * Captura la orden en PayPal dada el payPalOrderId
     */
    public CaptureResult captureOrder(String payPalOrderId) throws IOException, InterruptedException {
        String accessToken = getAccessToken();

        // Llamada a /v2/checkout/orders/{order_id}/capture
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v2/checkout/orders/" + payPalOrderId + "/capture"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString("{}")) // Body vacío
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            JsonNode json = mapper.readTree(response.body());

            // "status": "COMPLETED",
            // "purchase_units"[0]."payments"."captures"[0]."id"
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
            throw new RuntimeException("Error al capturar la orden PayPal: " + response.body());
        }
    }

    /**
     * POJO simple para devolver la info del capture
     */
    public record CaptureResult(String status, String captureId) {}
}