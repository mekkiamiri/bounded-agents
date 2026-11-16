package io.github.mekkiamiri.boundedagents.facturation.acl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.mekkiamiri.boundedagents.commande.api.CommandeValidee;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Facture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Payeur;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Payeurs;
import io.github.mekkiamiri.boundedagents.facturation.domaine.StatutFacture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.TauxTva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraducteurCommandeTest {

    private final Payeurs payeurs = new Payeurs();
    private final TraducteurCommande traducteur = new TraducteurCommande(payeurs);

    private static CommandeValidee commande(String compte, CommandeValidee.Ligne... lignes) {
        BigDecimal total = List.of(lignes).stream().map(CommandeValidee.Ligne::totalTTCRemise).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CommandeValidee("C-9", compte, "Acheteuse", List.of(lignes), total, LocalDate.of(2026, 12, 1), "D", "adresse", "");
    }

    @Test
    void l_acheteur_devient_le_payeur_rattache_a_son_compte() {
        Payeur societe = new Payeur("Société", "SIREN fictif", "1 rue du Siège");
        payeurs.rattacher("A-1", societe);
        Facture f = traducteur.traduire(commande("A-1", new CommandeValidee.Ligne("Café", "CAFE", 1, new BigDecimal("10.55"))), LocalDate.of(2026, 12, 2));
        assertEquals(societe, f.payeur());
        assertEquals("F-9", f.numero());
        assertEquals("C-9", f.referenceCommande());
    }

    @Test
    void le_ttc_devient_ht_plus_tva_selon_la_categorie() {
        payeurs.rattacher("A-1", new Payeur("P", "-", "-"));
        Facture f = traducteur.traduire(commande("A-1",
                new CommandeValidee.Ligne("Moulin", "MATERIEL", 1, new BigDecimal("89.00")),
                new CommandeValidee.Ligne("Café", "CAFE", 2, new BigDecimal("42.00"))), LocalDate.of(2026, 12, 2));
        assertEquals(TauxTva.NORMAL, f.lignes().get(0).taux());
        assertEquals(new BigDecimal("74.17"), f.lignes().get(0).montantHT());
        assertEquals(new BigDecimal("39.81"), f.lignes().get(1).montantHT());
        assertEquals(new BigDecimal("113.98"), f.montantHT());
        assertEquals(new BigDecimal("131.00"), f.montantTTC());
        assertEquals(LocalDate.of(2027, 1, 1), f.echeance());
        assertEquals(StatutFacture.EMISE, f.statut(LocalDate.of(2026, 12, 10)));
        assertEquals(StatutFacture.EN_RETARD, f.statut(LocalDate.of(2027, 1, 2)));
    }

    @Test
    void un_compte_sans_payeur_est_refuse_plutot_qu_invente() {
        assertThrows(TraducteurCommande.PayeurInconnu.class,
                () -> traducteur.traduire(commande("A-404", new CommandeValidee.Ligne("Café", "CAFE", 1, BigDecimal.TEN)), LocalDate.now()));
    }

    @Test
    void une_categorie_inconnue_est_refusee() {
        payeurs.rattacher("A-1", new Payeur("P", "-", "-"));
        assertThrows(IllegalArgumentException.class,
                () -> traducteur.traduire(commande("A-1", new CommandeValidee.Ligne("Tasse", "VAISSELLE", 1, BigDecimal.TEN)), LocalDate.now()));
    }
}
