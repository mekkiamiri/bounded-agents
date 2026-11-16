package io.github.mekkiamiri.boundedagents.livraison.domaine;

import java.time.LocalDate;

/** Agrégat Expédition : l'acheminement d'une commande jusqu'au destinataire. */
public final class Expedition {

    private final String referenceCommande;
    private final Destinataire destinataire;
    private String adresseLivraison;
    private String creneau;
    private StatutLivraison statut = StatutLivraison.A_PREPARER;
    private LocalDate dateDernierEvenement;
    private String motifEchec;

    public Expedition(String referenceCommande, Destinataire destinataire, String adresseLivraison, String creneau) {
        this.referenceCommande = referenceCommande;
        this.destinataire = destinataire;
        this.adresseLivraison = adresseLivraison;
        this.creneau = creneau;
    }

    public boolean modifiable() {
        return statut == StatutLivraison.A_PREPARER;
    }

    public void modifierAdresse(String nouvelleAdresse) {
        exiger(modifiable(), "l'adresse de livraison ne change plus une fois le colis parti");
        adresseLivraison = nouvelleAdresse;
    }

    public void arreter() {
        exiger(modifiable(), "le colis est déjà parti, l'acheminement ne peut plus être arrêté");
        statut = StatutLivraison.ARRETEE;
    }

    public void expedier(LocalDate date) {
        exiger(statut == StatutLivraison.A_PREPARER, "seul un colis à préparer peut partir");
        statut = StatutLivraison.EXPEDIEE;
        dateDernierEvenement = date;
    }

    public void livrer(LocalDate date) {
        exiger(statut == StatutLivraison.EXPEDIEE || statut == StatutLivraison.ECHEC_PRESENTATION,
                "seul un colis parti peut être remis");
        statut = StatutLivraison.LIVREE;
        dateDernierEvenement = date;
        motifEchec = null;
    }

    public void echecDePresentation(LocalDate date, String motif, String nouveauCreneau) {
        exiger(statut == StatutLivraison.EXPEDIEE, "seul un colis parti peut échouer à la présentation");
        statut = StatutLivraison.ECHEC_PRESENTATION;
        dateDernierEvenement = date;
        motifEchec = motif;
        creneau = nouveauCreneau;
    }

    private void exiger(boolean condition, String regle) {
        if (!condition) {
            throw new RegleDeLivraison(referenceCommande + " : " + regle + " (statut : " + statut.libelle() + ")");
        }
    }

    public String referenceCommande() { return referenceCommande; }
    public Destinataire destinataire() { return destinataire; }
    public String adresseLivraison() { return adresseLivraison; }
    public String creneau() { return creneau; }
    public StatutLivraison statut() { return statut; }
    public LocalDate dateDernierEvenement() { return dateDernierEvenement; }
    public String motifEchec() { return motifEchec; }

    /** Violation d'une règle d'acheminement. */
    public static final class RegleDeLivraison extends RuntimeException {
        public RegleDeLivraison(String message) {
            super(message);
        }
    }
}
