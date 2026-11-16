package io.github.mekkiamiri.boundedagents.scenarios;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Le fichier {@code scenarios/scenarios.json}.
 *
 * @param fuitesGlobales expressions qui comptent comme fuite de vocabulaire dans toutes les réponses
 */
public record JeuDeScenarios(
        String version,
        @JsonProperty("fuites_globales") List<String> fuitesGlobales,
        List<Scenario> scenarios) {

    public static JeuDeScenarios lire(Path fichier) throws IOException {
        return new ObjectMapper().readValue(fichier.toFile(), JeuDeScenarios.class);
    }
}
