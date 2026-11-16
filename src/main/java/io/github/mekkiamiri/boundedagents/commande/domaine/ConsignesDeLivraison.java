package io.github.mekkiamiri.boundedagents.commande.domaine;

/**
 * Ce que l'acheteur a saisi pour la livraison au moment de commander.
 * La Commande les recueille ; la Livraison en devient propriétaire dès la validation (partenariat).
 */
public record ConsignesDeLivraison(String nomDestinataire, String adresse, String creneauSouhaite) {
}
