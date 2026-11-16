package io.github.mekkiamiri.boundedagents.facturation.domaine;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Ligne de facture : montant hors taxes, TVA, et TTC qui en découle. */
public record LigneFacture(String libelle, int quantite, BigDecimal montantHT, TauxTva taux, BigDecimal montantTVA) {

    /** Construit une ligne à partir d'un TTC : le HT est arrondi au centime, la TVA est la différence. */
    public static LigneFacture depuisTTC(String libelle, int quantite, BigDecimal ttc, TauxTva taux) {
        BigDecimal diviseur = BigDecimal.ONE.add(taux.pourcent().movePointLeft(2));
        BigDecimal ht = ttc.divide(diviseur, 2, RoundingMode.HALF_UP);
        return new LigneFacture(libelle, quantite, ht, taux, ttc.subtract(ht));
    }

    public BigDecimal montantTTC() {
        return montantHT.add(montantTVA);
    }
}
