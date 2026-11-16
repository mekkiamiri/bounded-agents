package io.github.mekkiamiri.boundedagents.facturation.domaine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Dépôt en mémoire des factures, indexées par la référence de commande. */
public final class Factures {

    private final Map<String, Facture> parReferenceCommande = new LinkedHashMap<>();

    public void enregistrer(Facture facture) {
        parReferenceCommande.put(facture.referenceCommande(), facture);
    }

    public Optional<Facture> pourCommande(String referenceCommande) {
        return Optional.ofNullable(parReferenceCommande.get(referenceCommande));
    }
}
