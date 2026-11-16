package io.github.mekkiamiri.boundedagents.livraison.domaine;

/** Statut dans le langage de la Livraison : où en est l'acheminement. Ne dit rien du paiement. */
public enum StatutLivraison {
    A_PREPARER("à préparer"),
    EXPEDIEE("expédié"),
    LIVREE("livré"),
    ECHEC_PRESENTATION("en échec de présentation"),
    ARRETEE("arrêté");

    private final String libelle;

    StatutLivraison(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
