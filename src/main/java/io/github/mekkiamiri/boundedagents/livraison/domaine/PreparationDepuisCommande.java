package io.github.mekkiamiri.boundedagents.livraison.domaine;

import io.github.mekkiamiri.boundedagents.commande.api.CommandeValidee;

/**
 * Relation conformiste : la Livraison consomme l'événement publié {@link CommandeValidee} tel quel,
 * sans couche de traduction. Elle n'en reprend que les consignes de livraison et devient
 * propriétaire de l'adresse de livraison.
 *
 * <p>Le prix du conformisme : si la Commande change son langage publié, la Livraison suit.
 */
public final class PreparationDepuisCommande {

    private final Expeditions expeditions;

    public PreparationDepuisCommande(Expeditions expeditions) {
        this.expeditions = expeditions;
    }

    /** Une commande validée ouvre une expédition à préparer (une seule par commande). */
    public void surCommandeValidee(CommandeValidee evenement) {
        if (expeditions.pourCommande(evenement.numero()).isPresent()) {
            return;
        }
        expeditions.enregistrer(new Expedition(evenement.numero(), new Destinataire(evenement.nomDestinataireSaisi()),
                evenement.adresseLivraisonSaisie(), evenement.creneauSouhaite()));
    }
}
