package io.github.mekkiamiri.boundedagents.livraison.domaine;

import io.github.mekkiamiri.boundedagents.commande.api.CommandeValidee;
import io.github.mekkiamiri.boundedagents.commande.api.CommandesPubliees;
import java.util.Optional;

/**
 * Partenariat Commande ↔ Livraison : les deux équipes font évoluer ensemble le contrat
 * {@link CommandeValidee}. La Livraison le lit sans traduction lourde ; elle ne reprend
 * que les consignes de livraison et devient propriétaire de l'adresse de livraison.
 */
public final class PreparationDepuisCommande {

    private final CommandesPubliees commandes;
    private final Expeditions expeditions;

    public PreparationDepuisCommande(CommandesPubliees commandes, Expeditions expeditions) {
        this.commandes = commandes;
        this.expeditions = expeditions;
    }

    /** Vide si la commande n'est pas validée : pas d'expédition pour un brouillon. */
    public Optional<Expedition> preparer(String referenceCommande) {
        Optional<Expedition> existante = expeditions.pourCommande(referenceCommande);
        if (existante.isPresent()) {
            return existante;
        }
        return commandes.commandeValidee(referenceCommande).map(c -> {
            Expedition e = new Expedition(c.numero(), new Destinataire(c.nomDestinataireSaisi()),
                    c.adresseLivraisonSaisie(), c.creneauSouhaite());
            expeditions.enregistrer(e);
            return e;
        });
    }
}
