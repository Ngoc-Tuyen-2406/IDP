package com.idp.idpapi.ai.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.idpapi.ai.core.AiProperties;
import com.idp.idpapi.ai.service.AiGatewayService;

@Service
public class RestAiGatewayService implements AiGatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestAiGatewayService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(15);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public RestAiGatewayService(
            AiProperties aiProperties,
            ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.baseUrl = aiProperties.getBaseUrl().replaceFirst("/+$", "");
        this.objectMapper = objectMapper;
    }

    @Override
    public FullPipelinePayload processPipeline(PipelineRequest request) {
        return post("/api/v1/pipeline/process", request, FullPipelinePayload.class);
    }

    @Override
    public OcrPayload runOcr(PipelineRequest request) {
        return post("/api/v1/ocr/run", request, OcrPayload.class);
    }

    @Override
    public DetectionPayload runDetection(PipelineRequest request) {
        return post("/api/v1/detection/run", request, DetectionPayload.class);
    }

    @Override
    public MetadataPayload extractMetadata(PipelineRequest request) {
        return post("/api/v1/metadata/extract", request, MetadataPayload.class);
    }

    @Override
    public ClauseResultPayload extractClauses(PipelineRequest request) {
        return post("/api/v1/clauses/extract", request, ClauseResultPayload.class);
    }

    @Override
    public SummaryPayload generateSummary(SummaryRequest request) {
        return post("/api/v1/summary/generate", request, SummaryPayload.class);
    }

    @Override
    public RiskPayload analyzeRisk(RiskRequest request) {
        return post("/api/v1/risk/analyze", request, RiskPayload.class);
    }

    @Override
    public EmbeddingPayload createEmbedding(EmbeddingRequest request) {
        return post("/api/v1/embedding/create", request, EmbeddingPayload.class);
    }

    @Override
    public ChatPayload askQuestion(ChatRequest request) {
        return post("/api/v1/chat/ask", request, ChatPayload.class);
    }

    private <T, R> R post(String path, T body, Class<R> responseType) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            URI uri = URI.create(baseUrl + path);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            LOGGER.info("Calling AI Server POST {} with bodyBytes={}", uri, jsonBody.getBytes(StandardCharsets.UTF_8).length);
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "AI Server tra ve HTTP " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Khong the tao JSON gui toi AI Server.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Khong the ket noi toi AI Server.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Tac vu AI bi gian doan.", exception);
        }
    }
}
