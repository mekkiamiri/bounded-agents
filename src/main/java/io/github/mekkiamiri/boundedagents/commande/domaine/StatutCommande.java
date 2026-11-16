package io.github.mekkiamiri.boundedagents.commande.domaine;

/** Statut dans le langage de la Commande. Ne dit rien du paiement ni du colis. */
public enum StatutCommande {
    BROUILLON("brouillon"),
    VALIDEE("validée"),
    ANNULATION_DEMANDEE("annulation demandée"),
    ANNULEE("annulée");

    private final String libelle;

    StatutCommande(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
