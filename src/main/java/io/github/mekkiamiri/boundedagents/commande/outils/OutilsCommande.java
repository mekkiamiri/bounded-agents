package io.github.mekkiamiri.boundedagents.commande.outils;

import io.github.mekkiamiri.boundedagents.agent.OutilAgent;
import io.github.mekkiamiri.boundedagents.agent.Texte;
import io.github.mekkiamiri.boundedagents.commande.domaine.Commande;
import io.github.mekkiamiri.boundedagents.commande.domaine.Commandes;
import io.github.mekkiamiri.boundedagents.commande.domaine.LigneCommande;
import io.github.mekkiamiri.boundedagents.commande.domaine.StatutCommande;
import java.util.stream.Collectors;

/** Outils du sous-agent Commande. Vocabulaire : acheteur, panier, ligne, remise, montant TTC. */
public final class OutilsCommande {

    private final Commandes commandes;

    public OutilsCommande(Commandes commandes) {
        this.commandes = commandes;
    }

    @OutilAgent(nom = "consulterCommande",
            description = "Donne l'acheteur, les lignes du panier, la remise et le montant TTC d'une commande.")
    public String consulterCommande(String numero) {
        return commandes.trouver(numero).map(OutilsCommande::decrire).orElse(inconnue(numero));
    }

    @OutilAgent(nom = "statutCommande",
            description = "Donne le statut de la commande : brouillon, validée, annulation demandée ou annulée. "
                    + "Ne dit rien du paiement ni de l'acheminement.")
    public String statutCommande(String numero) {
        return commandes.trouver(numero)
                .map(c -> c.numero() + " : commande " + c.statut().libelle())
                .orElse(inconnue(numero));
    }

    @OutilAgent(nom = "demanderAnnulation",
            description = "Enregistre une demande d'annulation de la commande par l'acheteur. "
                    + "Un brouillon est annulé tout de suite ; une commande validée reste à confirmer.")
    public String demanderAnnulation(String numero) {
        return commandes.trouver(numero).map(c -> {
            StatutCommande s = c.demanderAnnulation();
            return switch (s) {
                case ANNULEE -> c.numero() + " : commande annulée";
                case ANNULATION_DEMANDEE -> c.numero()
                        + " : annulation demandée ; à confirmer (arrêt de l'acheminement, puis avoir éventuel)";
                default -> c.numero() + " : commande " + s.libelle();
            };
        }).orElse(inconnue(numero));
    }

    private static String decrire(Commande c) {
        String lignes = c.lignes().stream().map(OutilsCommande::ligne).collect(Collectors.joining(" ; "));
        return c.numero() + " : commande " + c.statut().libelle()
                + ", passée le " + c.datePassage()
                + " par l'acheteur " + c.acheteur().nom() + " (compte " + c.acheteur().compte() + ")"
                + ". Panier : " + lignes
                + ". Remise : " + c.remisePourcent() + " %"
                + ". Montant : " + Texte.euros(c.totalTTC()) + " TTC remise comprise.";
    }

    private static String ligne(LigneCommande l) {
        return l.quantite() + " x " + l.produit() + " à " + Texte.euros(l.prixUnitaireTTC()) + " TTC";
    }

    private static String inconnue(String numero) {
        return numero + " : aucune commande à ce numéro";
    }
}
