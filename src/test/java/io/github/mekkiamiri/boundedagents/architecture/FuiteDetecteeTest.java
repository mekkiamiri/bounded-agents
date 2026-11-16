package io.github.mekkiamiri.boundedagents.architecture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mekkiamiri.boundedagents.livraison.outils.OutilFuyant;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/** Les règles ne sont utiles que si elles échouent sur une vraie fuite : on le vérifie. */
class FuiteDetecteeTest {

    @Test
    void un_outil_de_livraison_qui_lit_la_facturation_est_refuse() {
        JavaClasses fuite = new ClassFileImporter().importClasses(OutilFuyant.class);
        AssertionError erreur = assertThrows(AssertionError.class, () -> Frontieres.outilsDuContexte("livraison").check(fuite));
        assertTrue(erreur.getMessage().contains("facturation.domaine.Factures"), erreur.getMessage());
        // Recopié tel quel dans les slides : on l'affiche dans la sortie de mvn.
        System.out.println(erreur.getMessage());
        assertThrows(AssertionError.class, () -> Frontieres.LIVRAISON_CONFORMISTE.check(fuite));
    }
}
