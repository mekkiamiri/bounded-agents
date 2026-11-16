package io.github.mekkiamiri.boundedagents.ia;

import io.github.mekkiamiri.boundedagents.agent.CatalogueOutils;
import io.github.mekkiamiri.boundedagents.demo.JeuDeDonnees;
import io.github.mekkiamiri.boundedagents.scenarios.JeuDeScenarios;
import io.github.mekkiamiri.boundedagents.scenarios.Notation;
import io.github.mekkiamiri.boundedagents.scenarios.Scenario;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

/**
 * Démo : S01 à S03 sur l'agent unique puis sur les agents découpés, avec un vrai modèle.
 *
 * <pre>
 * BA_MODELE=&lt;modèle&gt; BA_CLE_API=&lt;clé&gt; [BA_URL_BASE=&lt;API compatible OpenAI&gt;] mvn -q compile exec:java
 * </pre>
 * Une seule passe, pour voir : ce n'est pas le banc de mesure (12 scénarios × 5 passages, étape suivante).
 */
public final class Demonstration {

    private static final List<String> SCENARIOS_DEMO = List.of("S01", "S02", "S03");

    public static void main(String[] args) throws Exception {
        String nomModele = System.getenv("BA_MODELE");
        if (nomModele == null || nomModele.isBlank()) {
            System.err.println("Définir BA_MODELE (et BA_CLE_API, BA_URL_BASE si besoin). Aucun modèle n'est appelé sans.");
            System.exit(2);
        }
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder();
        options.model(nomModele);
        options.temperature(0.0);
        if (System.getenv("BA_CLE_API") != null) {
            options.apiKey(System.getenv("BA_CLE_API"));
        }
        if (System.getenv("BA_URL_BASE") != null) {
            options.baseUrl(System.getenv("BA_URL_BASE"));
        }
        ChatModel modele = OpenAiChatModel.builder().options(options.build()).build();
        jouer(modele, Path.of("."));
    }

    static void jouer(ChatModel modele, Path racine) throws Exception {
        JeuDeScenarios jeu = JeuDeScenarios.lire(racine.resolve("scenarios").resolve("scenarios.json"));
        Map<String, String> glossaires = Architectures.glossaires(racine);
        Notation notation = new Notation(jeu.fuitesGlobales());
        Map<String, BiFunction<ChatModel, CatalogueOutils, Agent>> architectures = Map.of(
                "agent unique", (m, c) -> Architectures.agentUnique(m, c, glossaires),
                "agents découpés", (m, c) -> Architectures.agentsDecoupes(m, c, glossaires));
        for (Scenario s : jeu.scenarios()) {
            if (!SCENARIOS_DEMO.contains(s.id())) {
                continue;
            }
            System.out.println("\n=== " + s.id() + " · " + s.question());
            for (String nom : List.of("agent unique", "agents découpés")) {
                ModeleCompte compte = new ModeleCompte(modele);
                Agent agent = architectures.get(nom).apply(compte, JeuDeDonnees.brulerie().catalogue());
                String reponse = agent.repondre(s.question());
                Notation.Resultat r = notation.noter(s, reponse);
                System.out.printf("--- %s : %s · fuites %d · appels modèle %d · tokens %d + %d%n%s%n",
                        nom, r.reussi() ? "réussi" : "échec", r.fuites().size(), compte.appels(),
                        compte.tokensEntree(), compte.tokensSortie(), reponse);
            }
        }
    }
}
