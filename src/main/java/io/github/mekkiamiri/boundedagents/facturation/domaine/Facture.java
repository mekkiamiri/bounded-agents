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

    public void encaisser(Paiement paiement) {
        if (paiement.montant().compareTo(resteARegler()) > 0) {
            throw new IllegalArgumentException(numero + " : paiement supérieur au reste à régler");
        }
        paiements.add(paiement);
    }

    public Avoir emettreAvoir(BigDecimal montantTTC, LocalDate date, String motif) {
        if (montantTTC.compareTo(montantTTC()) > 0) {
            throw new IllegalArgumentException(numero + " : avoir supérieur à la facture");
        }
        Avoir avoir = new Avoir("AV-" + referenceCommande.replace("C-", "") + "-" + (avoirs.size() + 1), montantTTC, date, motif);
        avoirs.add(avoir);
        return avoir;
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

    public BigDecimal resteARegler() {
        return montantTTC().subtract(dejaRegle());
    }

    public StatutFacture statut(LocalDate aujourdhui) {
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
