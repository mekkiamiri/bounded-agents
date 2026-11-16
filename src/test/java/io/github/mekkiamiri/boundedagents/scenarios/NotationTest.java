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

    @Test
    void un_nombre_se_compare_en_entier() {
        assertFalse(notation.noter(scenario("S12"), "Le montant est de 164,80 € TTC.").reussi());
        assertTrue(notation.noter(scenario("S12"), "64,80 €").reussi());
    }

    @Test
    void un_fragment_nie_ne_compte_pas() {
        assertFalse(Notation.affirme(Notation.normaliser("La commande est non validée."), "validée"));
        assertFalse(Notation.affirme(Notation.normaliser("Elle n'est pas encore validée."), "validée"));
        assertTrue(Notation.affirme(Notation.normaliser("Pas de souci : elle est validée."), "validée"));
        assertTrue(Notation.affirme(Notation.normaliser("Elle n'est pas validée."), "pas validée"));
    }

    @Test
    void une_erreur_niee_n_est_pas_une_erreur() {
        Notation.Resultat r = notation.noter(scenario("S06"),
                "Ce n'est pas un impayé : le destinataire était absent, nouvelle présentation le 2026-12-12.");
        assertTrue(r.reussi(), r.toString());
        assertTrue(r.erreurs().isEmpty());
    }

    @Test
    void la_negation_ne_traverse_pas_la_ponctuation() {
        assertTrue(Notation.affirme(Notation.normaliser("Pas entièrement. Le colis a été livré."), "livré"));
        assertFalse(Notation.affirme(Notation.normaliser("La commande est validée."), "valide"));
    }
}
