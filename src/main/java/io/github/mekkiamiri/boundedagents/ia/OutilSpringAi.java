package io.github.mekkiamiri.boundedagents.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.mekkiamiri.boundedagents.agent.Outil;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Adaptateur : un outil du catalogue ({@code @OutilAgent}) vu comme un {@link ToolCallback} Spring AI.
 * Le domaine et ses outils restent sans framework ; seul ce paquet connaît Spring AI.
 */
final class OutilSpringAi implements ToolCallback {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final Outil outil;
    private final List<String> parametres = new ArrayList<>();
    private final ToolDefinition definition;

    OutilSpringAi(Outil outil) {
        this.outil = outil;
        ObjectNode schema = JSON.createObjectNode().put("type", "object");
        ObjectNode proprietes = schema.putObject("properties");
        for (Parameter p : outil.methode().getParameters()) {
            if (!p.isNamePresent()) {
                throw new IllegalStateException("compiler avec -parameters : " + outil.nom());
            }
            parametres.add(p.getName());
            proprietes.putObject(p.getName()).put("type", "string");
        }
        parametres.forEach(schema.putArray("required")::add);
        schema.put("additionalProperties", false);
        this.definition = ToolDefinition.builder()
                .name(outil.nom())
                .description(outil.description())
                .inputSchema(schema.toString())
                .build();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return definition;
    }

    @Override
    public String call(String entree) {
        try {
            JsonNode n = JSON.readTree(entree == null || entree.isBlank() ? "{}" : entree);
            List<String> arguments = new ArrayList<>();
            for (String p : parametres) {
                JsonNode v = n.get(p);
                if (v == null || v.isNull()) {
                    return "erreur : argument manquant « " + p + " »";
                }
                arguments.add(v.asText());
            }
            return outil.appeler(arguments);
        } catch (Exception e) {
            return "erreur : " + e.getMessage();
        }
    }
}
