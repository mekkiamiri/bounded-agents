package io.github.mekkiamiri.boundedagents.livraison.outils;

import io.github.mekkiamiri.boundedagents.agent.OutilAgent;
import io.github.mekkiamiri.boundedagents.livraison.domaine.Expedition;
import io.github.mekkiamiri.boundedagents.livraison.domaine.Expeditions;

/** Outils du sous-agent Livraison. Vocabulaire : destinataire, colis, adresse de livraison, créneau, présentation. */
public final class OutilsLivraison {

    private final Expeditions expeditions;

    public OutilsLivraison(Expeditions expeditions) {
        this.expeditions = expeditions;
    }

    @OutilAgent(nom = "suivreExpedition",
            description = "Donne l'état d'acheminement du colis d'une référence de commande : destinataire, "
                    + "adresse de livraison, créneau, statut, date du dernier événement, motif d'échec.")
    public String suivreExpedition(String referenceCommande) {
        return expeditions.pourCommande(referenceCommande).map(OutilsLivraison::decrire).orElse(aucune(referenceCommande));
    }

    @OutilAgent(nom = "modifierAdresseLivraison",
            description = "Change l'adresse de livraison du colis tant qu'il n'est pas parti.")
    public String modifierAdresseLivraison(String referenceCommande, String nouvelleAdresse) {
        return expeditions.pourCommande(referenceCommande).map(e -> {
            if (!e.modifiable()) {
                return referenceCommande + " : modification impossible, colis " + e.statut().libelle()
                        + " le " + e.dateDernierEvenement();
            }
            e.modifierAdresse(nouvelleAdresse);
            return referenceCommande + " : adresse de livraison changée pour " + nouvelleAdresse;
        }).orElse(aucune(referenceCommande));
    }

    @OutilAgent(nom = "arreterExpedition",
            description = "Arrête l'acheminement du colis s'il n'est pas encore parti (préalable à une annulation).")
    public String arreterExpedition(String referenceCommande) {
        return expeditions.pourCommande(referenceCommande).map(e -> {
            if (!e.modifiable()) {
                return referenceCommande + " : arrêt impossible, colis " + e.statut().libelle()
                        + " le " + e.dateDernierEvenement() + " ; prévoir un retour";
            }
            e.arreter();
            return referenceCommande + " : acheminement arrêté, le colis ne partira pas";
        }).orElse(aucune(referenceCommande));
    }

    private static String decrire(Expedition e) {
        StringBuilder s = new StringBuilder(e.referenceCommande())
                .append(" : colis ").append(e.statut().libelle());
        if (e.dateDernierEvenement() != null) {
            s.append(" le ").append(e.dateDernierEvenement());
        }
        if (e.motifEchec() != null) {
            s.append(" (motif : ").append(e.motifEchec()).append(")");
        }
        s.append(". Destinataire : ").append(e.destinataire().nom())
                .append(". Adresse de livraison : ").append(e.adresseLivraison())
                .append(". Créneau de présentation : ").append(e.creneau()).append('.');
        return s.toString();
    }

    private static String aucune(String referenceCommande) {
        return referenceCommande + " : aucune expédition pour cette référence de commande";
    }
}
