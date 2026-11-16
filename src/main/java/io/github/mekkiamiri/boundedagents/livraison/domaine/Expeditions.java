package io.github.mekkiamiri.boundedagents.livraison.domaine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Dépôt en mémoire des expéditions, indexées par la référence de commande. */
public final class Expeditions {

    private final Map<String, Expedition> parReferenceCommande = new LinkedHashMap<>();

    public void enregistrer(Expedition expedition) {
        parReferenceCommande.put(expedition.referenceCommande(), expedition);
    }

    public Optional<Expedition> pourCommande(String referenceCommande) {
        return Optional.ofNullable(parReferenceCommande.get(referenceCommande));
    }
}
