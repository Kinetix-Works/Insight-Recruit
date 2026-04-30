package com.insight_recruit.backend.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight_recruit.backend.app.config.LlmProperties;
import com.insight_recruit.backend.app.dto.AiScoreResult;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OpenAiCompatibleScoringService implements LlmScoringService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final LlmProperties llmProperties;

    public OpenAiCompatibleScoringService(ObjectMapper objectMapper, LlmProperties llmProperties) {
        this.objectMapper = objectMapper;
        this.llmProperties = llmProperties;

        String baseUrl = llmProperties.baseUrl() == null || llmProperties.baseUrl().isBlank()
            ? "https://api.openai.com/v1"
            : llmProperties.baseUrl();

        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public AiScoreResult scoreCandidate(String jobDescription, String resumeText) {
        String apiKey = llmProperties.llmApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("LLM_API_KEY is not configured");
        }

        String model = llmProperties.model() == null || llmProperties.model().isBlank()
            ? "gpt-4o-mini"
            : llmProperties.model();

        double temperature = llmProperties.temperature() == null ? 0.2d : llmProperties.temperature();

        String prompt = buildPrompt(jobDescription, resumeText);
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "temperature", temperature,
            "response_format", Map.of("type", "json_object"),
            "messages", List.of(
                Map.of("role", "system", "content",
                    "You are an expert recruiting assistant. Return ONLY valid JSON."),
                Map.of("role", "user", "content", prompt)
            )
        );

        JsonNode root = webClient.post()
            .uri("/chat/completions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (root == null) {
            throw new IllegalStateException("LLM returned an empty response");
        }

        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.isNull()) {
            throw new IllegalStateException("LLM response is missing choices[0].message.content");
        }

        try {
            return objectMapper.readValue(contentNode.asText(), AiScoreResult.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to deserialize LLM scoring response", exception);
        }
    }

    private String buildPrompt(String jobDescription, String resumeText) {
        return """
            Analyze the resume against the job description.
            Return JSON ONLY with this exact schema:
            {
              "score": <integer 0-100>,
              "summary": "<concise 3-5 sentence summary>",
              "riskFlags": ["<risk flag 1>", "<risk flag 2>"]
            }

            Job Description:
            %s

            Resume Text:
            %s
            """.formatted(jobDescription, resumeText);
    }
}
