package io.github.mekkiamiri.boundedagents.commande.domaine;

import java.math.BigDecimal;

/** Une ligne du panier : prix affiché à l'acheteur, toutes taxes comprises. */
public record LigneCommande(String produit, Categorie categorie, int quantite, BigDecimal prixUnitaireTTC) {

    public enum Categorie { CAFE, MATERIEL }

    public BigDecimal totalTTC() {
        return prixUnitaireTTC.multiply(BigDecimal.valueOf(quantite));
    }
}
