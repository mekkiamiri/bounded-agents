package io.github.mekkiamiri.boundedagents.commande.domaine;

/** Langage de la Commande : la personne qui passe la commande depuis son compte. Ni payeur, ni destinataire. */
public record Acheteur(String nom, String compte) {
}
