package io.github.mekkiamiri.boundedagents;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mekkiamiri.boundedagents.agent.Outil;
import io.github.mekkiamiri.boundedagents.demo.JeuDeDonnees;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import io.github.mekkiamiri.boundedagents.scenarios.JeuDeScenarios;
import io.github.mekkiamiri.boundedagents.scenarios.Scenario;
import org.junit.jupiter.api.Test;

/** La page de démo recopie le catalogue d'outils : ce test l'empêche de dériver du code. */
class PageCoherenceTest {

    @Test
    void la_page_cite_chaque_outil_du_catalogue() throws IOException {
        String page = Files.readString(Path.of("docs", "index.html"));
        for (Outil o : JeuDeDonnees.brulerie().catalogue().tous()) {
            assertTrue(page.contains("'" + o.nom() + "'"), "docs/index.html ne cite pas l'outil " + o.nom());
        }
    }

    @Test
    void la_page_embarque_le_jeu_de_scenarios_a_jour() throws IOException {
        String page = Files.readString(Path.of("docs", "index.html"));
        for (Scenario s : JeuDeScenarios.lire(Path.of("scenarios", "scenarios.json")).scenarios()) {
            assertTrue(page.contains(s.question()) && page.contains(s.reponseAttendue()),
                    "docs/index.html n'a pas la version à jour du scénario " + s.id());
        }
    }
}
