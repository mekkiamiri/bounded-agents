package io.github.mekkiamiri.boundedagents.facturation.domaine;

/**
 * Langage de la Facturation : l'entité qui reçoit la facture et la règle.
 * Ce n'est pas forcément la personne qui a commandé (l'acheteur, côté Commande).
 *
 * @param adresseFacturation adresse légale portée sur la facture
 */
public record Payeur(String raisonSociale, String identifiantFiscal, String adresseFacturation) {
}
