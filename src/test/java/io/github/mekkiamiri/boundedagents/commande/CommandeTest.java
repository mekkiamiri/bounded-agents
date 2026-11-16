package io.github.mekkiamiri.boundedagents.commande;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mekkiamiri.boundedagents.commande.domaine.Acheteur;
import io.github.mekkiamiri.boundedagents.commande.domaine.Commande;
import io.github.mekkiamiri.boundedagents.commande.domaine.Commandes;
import io.github.mekkiamiri.boundedagents.commande.domaine.ConsignesDeLivraison;
import io.github.mekkiamiri.boundedagents.commande.domaine.LigneCommande;
import io.github.mekkiamiri.boundedagents.commande.domaine.StatutCommande;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandeTest {

    private static Commande commande(int remise) {
        return new Commande("C-1", new Acheteur("A", "A-1"),
                List.of(new LigneCommande("Café", LigneCommande.Categorie.CAFE, 3, new BigDecimal("24.00"))),
                remise, LocalDate.of(2026, 1, 1), new ConsignesDeLivraison("D", "adresse", "créneau"));
    }

    @Test
    void le_montant_est_ttc_remise_comprise() {
        assertEquals(new BigDecimal("64.80"), commande(10).totalTTC());
        assertEquals(new BigDecimal("72.00"), commande(0).totalTTC());
    }

    @Test
    void annuler_un_brouillon_est_immediat_annuler_une_commande_validee_est_une_demande() {
        assertEquals(StatutCommande.ANNULEE, commande(0).demanderAnnulation());
        Commande validee = commande(0);
        validee.valider();
        assertEquals(StatutCommande.ANNULATION_DEMANDEE, validee.demanderAnnulation());
    }

    @Test
    void seules_les_commandes_validees_sont_publiees() {
        Commandes commandes = new Commandes();
        Commande c = commande(10);
        commandes.enregistrer(c);
        assertTrue(commandes.commandeValidee("C-1").isEmpty());
        c.valider();
        assertEquals(new BigDecimal("64.80"), commandes.commandeValidee("C-1").orElseThrow().totalTTC());
    }

    @Test
    void une_commande_validee_ne_se_revalide_pas() {
        Commande c = commande(0);
        c.valider();
        assertThrows(IllegalStateException.class, c::valider);
    }
}
