package io.github.mekkiamiri.boundedagents.facturation.outils;

import io.github.mekkiamiri.boundedagents.agent.OutilAgent;
import io.github.mekkiamiri.boundedagents.agent.Texte;
import io.github.mekkiamiri.boundedagents.facturation.acl.EmissionDesFactures;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Avoir;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Facture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Factures;
import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

/** Outils du sous-agent Facturation. Vocabulaire : payeur, facture, HT, TVA, TTC, échéance, avoir. */
public final class OutilsFacturation {

    private final Factures factures;
    private final EmissionDesFactures emission;
    private final Clock horloge;

    public OutilsFacturation(Factures factures, EmissionDesFactures emission, Clock horloge) {
        this.factures = factures;
        this.emission = emission;
        this.horloge = horloge;
    }

    @OutilAgent(nom = "consulterFacture",
            description = "Donne la facture liée à une référence de commande : payeur, adresse de facturation, "
                    + "montants HT, TVA et TTC, échéance et statut de paiement.")
    public String consulterFacture(String referenceCommande) {
        return factures.pourCommande(referenceCommande).map(this::decrire).orElse(aucune(referenceCommande));
    }

    @OutilAgent(nom = "resteARegler",
            description = "Donne le montant TTC qui reste à régler par le payeur sur la facture d'une référence de commande.")
    public String resteARegler(String referenceCommande) {
        return factures.pourCommande(referenceCommande)
                .map(f -> f.numero() + " : reste à régler " + Texte.euros(f.resteARegler()) + " TTC sur "
                        + Texte.euros(f.montantTTC()) + " TTC (déjà réglé " + Texte.euros(f.dejaRegle())
                        + "), échéance " + f.echeance())
                .orElse(aucune(referenceCommande));
    }

    @OutilAgent(nom = "listerAvoirs",
            description = "Liste les avoirs émis sur la facture d'une référence de commande (montant TTC, date, motif).")
    public String listerAvoirs(String referenceCommande) {
        return factures.pourCommande(referenceCommande).map(f -> {
            if (f.avoirs().isEmpty()) {
                return f.numero() + " : aucun avoir";
            }
            return f.numero() + " : " + f.avoirs().stream().map(OutilsFacturation::avoir).collect(Collectors.joining(" ; "));
        }).orElse(aucune(referenceCommande));
    }

    @OutilAgent(nom = "emettreFacture",
            description = "Émet la facture d'une commande validée (une seule par référence) et renvoie son numéro.")
    public String emettreFacture(String referenceCommande) {
        return emission.emettre(referenceCommande, aujourdhui())
                .map(f -> f.numero() + " : facture émise pour " + f.payeur().raisonSociale() + ", "
                        + Texte.euros(f.montantTTC()) + " TTC")
                .orElse(referenceCommande + " : aucune commande validée à cette référence, rien à facturer");
    }

    private String decrire(Facture f) {
        return f.numero() + " (commande " + f.referenceCommande() + ") : facture " + f.statut(aujourdhui()).libelle()
                + ". Payeur : " + f.payeur().raisonSociale() + " (" + f.payeur().identifiantFiscal() + ")"
                + ", adresse de facturation : " + f.payeur().adresseFacturation()
                + ". Montant : " + Texte.euros(f.montantHT()) + " HT + " + Texte.euros(f.montantTVA()) + " de TVA = "
                + Texte.euros(f.montantTTC()) + " TTC. Émise le " + f.dateEmission() + ", échéance " + f.echeance()
                + ". Reste à régler : " + Texte.euros(f.resteARegler()) + ".";
    }

    private static String avoir(Avoir a) {
        return "avoir " + a.numero() + " de " + Texte.euros(a.montantTTC()) + " TTC émis le " + a.date() + " (" + a.motif() + ")";
    }

    private LocalDate aujourdhui() {
        return LocalDate.now(horloge);
    }

    private static String aucune(String referenceCommande) {
        return referenceCommande + " : aucune facture pour cette référence de commande";
    }
}
