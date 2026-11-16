package io.github.mekkiamiri.boundedagents.scenarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mekkiamiri.boundedagents.agent.CatalogueOutils;
import io.github.mekkiamiri.boundedagents.agent.Outil;
import io.github.mekkiamiri.boundedagents.demo.JeuDeDonnees;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Le jeu de scénarios est prouvé par le domaine : chaque réponse attendue repose sur des appels d'outils réels. */
class ScenariosTest {

    private static JeuDeScenarios jeu;

    @BeforeAll
    static void lire() throws IOException {
        jeu = JeuDeScenarios.lire(Path.of("scenarios", "scenarios.json"));
    }

    static Stream<Scenario> scenarios() throws IOException {
        return JeuDeScenarios.lire(Path.of("scenarios", "scenarios.json")).scenarios().stream();
    }

    @Test
    void douze_scenarios_aux_identifiants_uniques() {
        assertEquals(12, jeu.scenarios().size());
        assertEquals(12, jeu.scenarios().stream().map(Scenario::id).distinct().count());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void les_verifications_passent_sur_le_jeu_de_donnees(Scenario s) {
        Set<String> contextesUtilises = new HashSet<>();
        for (Scenario.Verification v : s.verifications()) {
            // Jeu de données neuf à chaque appel : certains outils modifient l'état (annulation, adresse).
            CatalogueOutils catalogue = JeuDeDonnees.brulerie().catalogue();
            Outil outil = catalogue.outil(v.outil()).orElseThrow(() -> new AssertionError(s.id() + " : outil inconnu " + v.outil()));
            contextesUtilises.add(outil.contexte());
            String sortie = outil.appeler(v.arguments());
            assertTrue(Notation.normaliser(sortie).contains(Notation.normaliser(v.contient())),
                    s.id() + " / " + v.outil() + " : attendu « " + v.contient() + " », obtenu « " + sortie + " »");
        }
        assertTrue(s.contextes().containsAll(contextesUtilises), s.id() + " : contextes " + contextesUtilises + " non déclarés");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void la_reponse_attendue_reussit_sans_fuite(Scenario s) {
        Notation.Resultat r = new Notation(jeu.fuitesGlobales()).noter(s, s.reponseAttendue());
        assertTrue(r.reussi(), s.id() + " : " + r);
        assertEquals(0, r.fuites().size(), s.id() + " : " + r.fuites());
        assertFalse(s.piege().isBlank());
    }
}
