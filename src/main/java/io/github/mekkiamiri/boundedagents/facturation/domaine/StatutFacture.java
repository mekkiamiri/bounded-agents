package io.github.mekkiamiri.boundedagents.facturation.domaine;

/** Statut dans le langage de la Facturation. Ne dit rien de l'acheminement des colis. */
public enum StatutFacture {
    EMISE("émise"),
    PARTIELLEMENT_PAYEE("partiellement payée"),
    PAYEE("payée"),
    EN_RETARD("en retard"),
    ANNULEE_PAR_AVOIR("annulée par avoir");

    private final String libelle;

    StatutFacture(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
