package io.github.mekkiamiri.boundedagents.facturation.domaine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Agrégat Facture. Une facture par commande validée ; échéance à 30 jours. */
public final class Facture {

    public static final int DELAI_PAIEMENT_JOURS = 30;

    private final String numero;
    private final String referenceCommande;
    private final Payeur payeur;
    private final List<LigneFacture> lignes;
    private final LocalDate dateEmission;
    private final List<Paiement> paiements = new ArrayList<>();
    private final List<Avoir> avoirs = new ArrayList<>();

    public Facture(String numero, String referenceCommande, Payeur payeur, List<LigneFacture> lignes, LocalDate dateEmission) {
        this.numero = numero;
        this.referenceCommande = referenceCommande;
        this.payeur = payeur;
        this.lignes = List.copyOf(lignes);
        this.dateEmission = dateEmission;
    }

    /** Encaisse un paiement du payeur : strictement positif, jamais au-delà du reste à régler. */
    public void encaisser(Paiement paiement) {
        exigerPositif(paiement.montant(), "paiement");
        if (paiement.montant().compareTo(resteARegler()) > 0) {
            throw new IllegalArgumentException(numero + " : paiement supérieur au reste à régler");
        }
        paiements.add(paiement);
    }

    /**
     * Émet un avoir : strictement positif, plafonné à ce qui n'a pas déjà été annulé par d'autres avoirs.
     * L'avoir réduit d'abord le reste à régler ; ce qui dépasse devient un trop-perçu à rembourser au payeur.
     */
    public Avoir emettreAvoir(BigDecimal montantTTC, LocalDate date, String motif) {
        exigerPositif(montantTTC, "avoir");
        if (montantTTC.compareTo(montantNetTTC()) > 0) {
            throw new IllegalArgumentException(numero + " : avoir supérieur au montant non encore annulé ("
                    + montantNetTTC().toPlainString() + ")");
        }
        Avoir avoir = new Avoir("AV-" + referenceCommande.replace("C-", "") + "-" + (avoirs.size() + 1), montantTTC, date, motif);
        avoirs.add(avoir);
        return avoir;
    }

    private void exigerPositif(BigDecimal montant, String quoi) {
        if (montant.signum() <= 0) {
            throw new IllegalArgumentException(numero + " : un " + quoi + " est strictement positif");
        }
    }

    public LocalDate echeance() {
        return dateEmission.plusDays(DELAI_PAIEMENT_JOURS);
    }

    public BigDecimal montantHT() {
        return lignes.stream().map(LigneFacture::montantHT).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal montantTVA() {
        return lignes.stream().map(LigneFacture::montantTVA).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal montantTTC() {
        return montantHT().add(montantTVA());
    }

    public BigDecimal dejaRegle() {
        return paiements.stream().map(Paiement::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalAvoirs() {
        return avoirs.stream().map(Avoir::montantTTC).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Ce que le payeur doit au total : TTC moins les avoirs émis. */
    public BigDecimal montantNetTTC() {
        return montantTTC().subtract(totalAvoirs());
    }

    /** Ce qui reste dû par le payeur (jamais négatif). */
    public BigDecimal resteARegler() {
        return montantNetTTC().subtract(dejaRegle()).max(BigDecimal.ZERO);
    }

    /** Trop-perçu à rendre au payeur quand un avoir arrive après le paiement. */
    public BigDecimal aRembourser() {
        return dejaRegle().subtract(montantNetTTC()).max(BigDecimal.ZERO);
    }

    public StatutFacture statut(LocalDate aujourdhui) {
        if (montantNetTTC().signum() == 0) {
            return StatutFacture.ANNULEE_PAR_AVOIR;
        }
        if (resteARegler().signum() == 0) {
            return StatutFacture.PAYEE;
        }
        if (aujourdhui.isAfter(echeance())) {
            return StatutFacture.EN_RETARD;
        }
        return dejaRegle().signum() > 0 ? StatutFacture.PARTIELLEMENT_PAYEE : StatutFacture.EMISE;
    }

    public String numero() { return numero; }
    public String referenceCommande() { return referenceCommande; }
    public Payeur payeur() { return payeur; }
    public List<LigneFacture> lignes() { return lignes; }
    public LocalDate dateEmission() { return dateEmission; }
    public List<Avoir> avoirs() { return List.copyOf(avoirs); }
}
