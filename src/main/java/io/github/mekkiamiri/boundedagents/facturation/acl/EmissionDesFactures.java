package io.github.mekkiamiri.boundedagents.facturation.acl;

import io.github.mekkiamiri.boundedagents.commande.api.CommandeValidee;
import io.github.mekkiamiri.boundedagents.commande.api.CommandesPubliees;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Facture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Factures;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Émet la facture d'une commande validée en passant par la couche anticorruption. Idempotent.
 * Deux entrées : l'événement {@link CommandeValidee} (cas normal) et une demande explicite (outil).
 */
public final class EmissionDesFactures {

    private final CommandesPubliees commandes;
    private final TraducteurCommande traducteur;
    private final Factures factures;

    public EmissionDesFactures(CommandesPubliees commandes, TraducteurCommande traducteur, Factures factures) {
        this.commandes = commandes;
        this.traducteur = traducteur;
        this.factures = factures;
    }

    /** Abonné à l'événement de la Commande : la facture est émise le jour de la validation. */
    public void surCommandeValidee(CommandeValidee evenement) {
        if (factures.pourCommande(evenement.numero()).isEmpty()) {
            factures.enregistrer(traducteur.traduire(evenement, evenement.dateValidation()));
        }
    }

    /** Vide si aucune commande validée ne porte cette référence. */
    public Optional<Facture> emettre(String referenceCommande, LocalDate dateEmission) {
        Optional<Facture> existante = factures.pourCommande(referenceCommande);
        if (existante.isPresent()) {
            return existante;
        }
        return commandes.commandeValidee(referenceCommande).map(c -> {
            Facture f = traducteur.traduire(c, dateEmission);
            factures.enregistrer(f);
            return f;
        });
    }
}
