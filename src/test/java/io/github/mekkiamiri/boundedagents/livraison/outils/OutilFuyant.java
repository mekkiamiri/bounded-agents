package io.github.mekkiamiri.boundedagents.livraison.outils;

import io.github.mekkiamiri.boundedagents.agent.OutilAgent;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Factures;

/**
 * Exemple volontairement faux (sources de test uniquement) : un outil de la Livraison qui va lire
 * le modèle de la Facturation pour « rendre service ». C'est la fuite de vocabulaire qu'ArchUnit doit refuser.
 */
public final class OutilFuyant {

    private final Factures factures;

    public OutilFuyant(Factures factures) {
        this.factures = factures;
    }

    @OutilAgent(nom = "suivreColisEtPaiement", description = "Dit si le colis est parti et si le client a payé.")
    public String suivreColisEtPaiement(String referenceCommande) {
        return factures.pourCommande(referenceCommande).map(f -> f.resteARegler().toPlainString()).orElse("?");
    }
}
