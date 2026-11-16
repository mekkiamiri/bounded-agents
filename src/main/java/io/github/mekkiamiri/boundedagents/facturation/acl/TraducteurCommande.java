package io.github.mekkiamiri.boundedagents.facturation.acl;

import io.github.mekkiamiri.boundedagents.commande.api.CommandeValidee;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Facture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.LigneFacture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Payeur;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Payeurs;
import io.github.mekkiamiri.boundedagents.facturation.domaine.TauxTva;
import java.time.LocalDate;
import java.util.List;

/**
 * Couche anticorruption : traduit le langage publié de la Commande dans celui de la Facturation.
 *
 * <ul>
 *   <li>acheteur (compte) → payeur (raison sociale, identifiant fiscal, adresse de facturation) ;</li>
 *   <li>ligne TTC remisée → ligne HT + TVA, au taux qui dépend de la catégorie ;</li>
 *   <li>numéro de commande → simple référence portée par la facture.</li>
 * </ul>
 * C'est le seul endroit de la Facturation qui connaît la Commande.
 */
public final class TraducteurCommande {

    private final Payeurs payeurs;

    public TraducteurCommande(Payeurs payeurs) {
        this.payeurs = payeurs;
    }

    public Facture traduire(CommandeValidee commande, LocalDate dateEmission) {
        Payeur payeur = payeurs.pourCompte(commande.compteAcheteur())
                .orElseThrow(() -> new PayeurInconnu(commande.compteAcheteur()));
        List<LigneFacture> lignes = commande.lignes().stream()
                .map(l -> LigneFacture.depuisTTC(l.produit(), l.quantite(), l.totalTTCRemise(), taux(l.categorie())))
                .toList();
        return new Facture("F-" + commande.numero().replace("C-", ""), commande.numero(), payeur, lignes, dateEmission);
    }

    static TauxTva taux(String categorie) {
        return switch (categorie) {
            case "CAFE" -> TauxTva.REDUIT;
            case "MATERIEL" -> TauxTva.NORMAL;
            default -> throw new IllegalArgumentException("catégorie sans taux de TVA connu : " + categorie);
        };
    }

    /** Le compte de l'acheteur n'est rattaché à aucun payeur : on refuse plutôt que d'inventer. */
    public static final class PayeurInconnu extends RuntimeException {
        public PayeurInconnu(String compte) {
            super("aucun payeur rattaché au compte " + compte);
        }
    }
}
