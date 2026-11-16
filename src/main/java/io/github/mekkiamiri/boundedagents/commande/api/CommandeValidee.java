package io.github.mekkiamiri.boundedagents.commande.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Événement du domaine publié par la Commande, dans son langage publié : « l'acheteur a validé sa commande ».
 *
 * <p>C'est le seul point d'entrée vers la Commande (service hôte ouvert). La Livraison le consomme
 * tel quel (conformiste) ; la Facturation le traduit dans sa couche anticorruption.
 * À l'étape suivante, c'est ce même événement qui voyagera dans un message A2A.
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
        LocalDate dateValidation,
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
