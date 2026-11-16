package io.github.mekkiamiri.boundedagents.scenarios;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Un scénario métier : une question posée en langage courant, la réponse attendue et de quoi la noter.
 *
 * @param doitContenir groupes d'alternatives : chaque groupe doit apparaître (au moins une de ses variantes)
 * @param neDoitPasContenir erreurs factuelles qui font échouer la réponse
 * @param fuites expressions qui trahissent un mélange de vocabulaires (comptées, sans faire échouer)
 * @param verifications appels d'outils qui prouvent la réponse attendue sur le jeu de données
 */
public record Scenario(
        String id,
        String question,
        String piege,
        List<String> contextes,
        @JsonProperty("reponse_attendue") String reponseAttendue,
        @JsonProperty("doit_contenir") List<List<String>> doitContenir,
        @JsonProperty("ne_doit_pas_contenir") List<String> neDoitPasContenir,
        List<String> fuites,
        List<Verification> verifications) {

    public record Verification(String outil, List<String> arguments, String contient) {
    }
}
