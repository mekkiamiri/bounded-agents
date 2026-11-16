package io.github.mekkiamiri.boundedagents.agent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Les deux architectures comparées par le talk.
 *
 * <ul>
 *   <li>{@link #agentUnique} : un seul agent voit tous les outils de tous les contextes ;</li>
 *   <li>{@link #agentsDecoupes} : un sous-agent par contexte borné, qui ne voit que ses outils.</li>
 * </ul>
 *
 * <p>La taille des descriptions est mesurée en caractères (pas en tokens : le comptage des
 * tokens dépend du modèle et sera fait à l'étape suivante).
 */
public record Configuration(String nom, Map<String, List<Outil>> agents) {

    public static Configuration agentUnique(CatalogueOutils catalogue) {
        return new Configuration("agent unique", Map.of("unique", catalogue.tous()));
    }

    public static Configuration agentsDecoupes(CatalogueOutils catalogue) {
        return new Configuration("agents découpés", catalogue.parContexte());
    }

    /** Nombre maximal d'outils qu'un agent doit départager dans cette configuration. */
    public int outilsParAgentMax() {
        return agents.values().stream().mapToInt(List::size).max().orElse(0);
    }

    /** Caractères de descriptions d'outils que l'agent le plus chargé reçoit dans son contexte. */
    public int caracteresDescriptionsMax() {
        return agents.values().stream()
                .mapToInt(l -> l.stream().mapToInt(o -> o.nom().length() + o.description().length()).sum())
                .max().orElse(0);
    }

    public String resume() {
        return nom + " : " + agents.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().size())
                .collect(Collectors.joining(", "));
    }
}
