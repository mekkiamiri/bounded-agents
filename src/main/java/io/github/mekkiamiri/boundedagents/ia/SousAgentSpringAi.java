package io.github.mekkiamiri.boundedagents.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Un sous-agent vu par le routeur comme un outil : « pose ta question au contexte X ».
 * En v0 l'appel est local ; à l'étape suivante, ce même échange passera par A2A.
 */
final class SousAgentSpringAi implements ToolCallback {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final Agent sousAgent;
    private final ToolDefinition definition;

    SousAgentSpringAi(String nom, String description, Agent sousAgent) {
        this.sousAgent = sousAgent;
        this.definition = ToolDefinition.builder()
                .name(nom)
                .description(description)
                .inputSchema("{\"type\":\"object\",\"properties\":{\"question\":{\"type\":\"string\"}},"
                        + "\"required\":[\"question\"],\"additionalProperties\":false}")
                .build();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return definition;
    }

    @Override
    public String call(String entree) {
        try {
            JsonNode n = JSON.readTree(entree);
            JsonNode q = n.get("question");
            if (q == null || q.asText().isBlank()) {
                return "erreur : argument manquant « question »";
            }
            return sousAgent.repondre(q.asText());
        } catch (Exception e) {
            return "erreur : " + e.getMessage();
        }
    }
}
