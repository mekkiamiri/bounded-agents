package io.github.mekkiamiri.boundedagents.scenarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * La notation sur des réponses écrites à la main pour le test (ce ne sont pas des réponses de modèles).
 */
class NotationTest {

    private final JeuDeScenarios jeu;
    private final Notation notation;

    NotationTest() throws IOException {
        jeu = JeuDeScenarios.lire(Path.of("scenarios", "scenarios.json"));
        notation = new Notation(jeu.fuitesGlobales());
    }

    private Scenario scenario(String id) {
        return jeu.scenarios().stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void une_reponse_qui_ne_regarde_qu_un_statut_echoue() {
        Notation.Resultat r = notation.noter(scenario("S01"), "Oui, elle est terminée : le client l'a reçue le 6 novembre.");
        assertFalse(r.reussi());
        assertEquals(1, r.erreurs().size());
        assertEquals(1, r.fuites().size());
    }

    @Test
    void une_seule_adresse_est_une_reponse_incomplete_et_une_fuite() {
        Notation.Resultat r = notation.noter(scenario("S02"), "L'adresse du client est 12 rue des Lilas, 69003 Lyon.");
        assertFalse(r.reussi());
        assertEquals(1, r.manquants().size());
        assertEquals(2, r.fuites().size());
    }

    @Test
    void accents_et_majuscules_sont_ignores() {
        Notation.Resultat r = notation.noter(scenario("S04"), "Il RESTE 81,00 EUR a regler.");
        assertTrue(r.reussi());
    }
}
