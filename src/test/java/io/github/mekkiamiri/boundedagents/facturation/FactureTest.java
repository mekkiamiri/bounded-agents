package io.github.mekkiamiri.boundedagents.facturation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.mekkiamiri.boundedagents.facturation.domaine.Facture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.LigneFacture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Paiement;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Payeur;
import io.github.mekkiamiri.boundedagents.facturation.domaine.StatutFacture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.TauxTva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FactureTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 12, 1);

    private static Facture facture() {
        return new Facture("F-1", "C-1", new Payeur("P", "-", "-"),
                List.of(LigneFacture.depuisTTC("Café", 2, new BigDecimal("23.00"), TauxTva.REDUIT)), JOUR);
    }

    private static BigDecimal e(String montant) {
        return new BigDecimal(montant);
    }

    @Test
    void les_avoirs_sont_plafonnes_a_ce_qui_n_est_pas_deja_annule() {
        Facture f = facture();
        f.emettreAvoir(e("15.00"), JOUR, "un");
        assertThrows(IllegalArgumentException.class, () -> f.emettreAvoir(e("8.01"), JOUR, "deux"));
        f.emettreAvoir(e("8.00"), JOUR, "deux");
        assertEquals(StatutFacture.ANNULEE_PAR_AVOIR, f.statut(JOUR));
        assertEquals(0, f.resteARegler().signum());
    }

    @Test
    void un_avoir_reduit_le_reste_a_regler() {
        Facture f = facture();
        f.emettreAvoir(e("11.50"), JOUR, "abîmé");
        assertEquals(e("11.50"), f.resteARegler());
        assertThrows(IllegalArgumentException.class, () -> f.encaisser(new Paiement(e("11.51"), JOUR)));
        f.encaisser(new Paiement(e("11.50"), JOUR));
        assertEquals(StatutFacture.PAYEE, f.statut(JOUR));
    }

    @Test
    void un_avoir_apres_paiement_devient_un_trop_percu_a_rembourser() {
        Facture f = facture();
        f.encaisser(new Paiement(e("23.00"), JOUR));
        f.emettreAvoir(e("11.50"), JOUR, "abîmé");
        assertEquals(0, f.resteARegler().signum());
        assertEquals(e("11.50"), f.aRembourser());
        assertEquals(StatutFacture.PAYEE, f.statut(JOUR));
    }

    @Test
    void paiements_et_avoirs_negatifs_ou_nuls_sont_refuses() {
        Facture f = facture();
        assertThrows(IllegalArgumentException.class, () -> f.encaisser(new Paiement(e("-5.00"), JOUR)));
        assertThrows(IllegalArgumentException.class, () -> f.encaisser(new Paiement(BigDecimal.ZERO, JOUR)));
        assertThrows(IllegalArgumentException.class, () -> f.emettreAvoir(e("-1.00"), JOUR, "?"));
    }
}
