package io.github.mekkiamiri.boundedagents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.mekkiamiri.boundedagents.demo.DonneesPage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** La page lit docs/donnees.js, généré depuis le code, les glossaires et les scénarios : il ne doit pas dériver. */
class PageCoherenceTest {

    @Test
    void les_donnees_de_la_page_sont_a_jour() throws IOException {
        String attendu = DonneesPage.generer(Path.of("."));
        if (Boolean.getBoolean("regenererPage")) {
            Files.writeString(DonneesPage.FICHIER, attendu, StandardCharsets.UTF_8);
        }
        assertEquals(attendu, Files.readString(DonneesPage.FICHIER, StandardCharsets.UTF_8),
                "docs/donnees.js est périmé : mvn -B test -Dtest=PageCoherenceTest -DregenererPage=true");
    }

    @Test
    void la_page_ne_recopie_plus_les_donnees_a_la_main() throws IOException {
        String page = Files.readString(Path.of("docs", "index.html"), StandardCharsets.UTF_8);
        assertFalse(page.contains("\"reponse_attendue\""), "les scénarios viennent de donnees.js");
        assertFalse(page.contains("'consulterCommande'"), "les outils viennent de donnees.js");
    }
}
