package io.github.mekkiamiri.boundedagents.commande.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Langage publié par la Commande : ce que les autres contextes ont le droit de savoir d'une commande validée.
 *
 * <p>Ce contrat est le seul point d'entrée vers la Commande. La Livraison le lit directement
 * (partenariat) ; la Facturation le traduit dans sa couche anticorruption.
 *
 * @param compteAcheteur compte de l'acheteur (la Facturation en déduit le payeur)
 * @param lignes lignes avec leur total TTC remise comprise
 */
public record CommandeValidee(
        String numero,
        String compteAcheteur,
        String nomAcheteur,
        List<Ligne> lignes,
        BigDecimal totalTTC,
        LocalDate datePassage,
        String nomDestinataireSaisi,
        String adresseLivraisonSaisie,
        String creneauSouhaite) {

    public CommandeValidee {
        lignes = List.copyOf(lignes);
    }

    /** @param categorie {@code CAFE} ou {@code MATERIEL} */
    public record Ligne(String produit, String categorie, int quantite, BigDecimal totalTTCRemise) {
    }
}
