package io.github.mekkiamiri.boundedagents.commande.domaine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Agrégat Commande : un panier validé (ou non) par un acheteur. */
public final class Commande {

    private final String numero;
    private final Acheteur acheteur;
    private final List<LigneCommande> lignes;
    private final int remisePourcent;
    private final LocalDate datePassage;
    private final ConsignesDeLivraison consignes;
    private StatutCommande statut;

    public Commande(String numero, Acheteur acheteur, List<LigneCommande> lignes, int remisePourcent,
                    LocalDate datePassage, ConsignesDeLivraison consignes) {
        if (lignes.isEmpty()) {
            throw new IllegalArgumentException("une commande a au moins une ligne");
        }
        if (remisePourcent < 0 || remisePourcent > 100) {
            throw new IllegalArgumentException("remise hors bornes : " + remisePourcent);
        }
        this.numero = numero;
        this.acheteur = acheteur;
        this.lignes = List.copyOf(lignes);
        this.remisePourcent = remisePourcent;
        this.datePassage = datePassage;
        this.consignes = consignes;
        this.statut = StatutCommande.BROUILLON;
    }

    public void valider() {
        if (statut != StatutCommande.BROUILLON) {
            throw new IllegalStateException(numero + " : seule une commande en brouillon se valide");
        }
        statut = StatutCommande.VALIDEE;
    }

    /**
     * Demande d'annulation. Un brouillon est annulé tout de suite ; une commande validée passe en
     * « annulation demandée » : la Commande ne sait pas si le colis est parti, c'est à la Livraison de le dire.
     */
    public StatutCommande demanderAnnulation() {
        statut = switch (statut) {
            case BROUILLON -> StatutCommande.ANNULEE;
            case VALIDEE -> StatutCommande.ANNULATION_DEMANDEE;
            case ANNULATION_DEMANDEE, ANNULEE -> statut;
        };
        return statut;
    }

    /** Prix d'une ligne pour l'acheteur, remise comprise, arrondi au centime. */
    public BigDecimal totalTTCRemise(LigneCommande ligne) {
        BigDecimal coefficient = BigDecimal.valueOf(100 - remisePourcent).movePointLeft(2);
        return ligne.totalTTC().multiply(coefficient).setScale(2, RoundingMode.HALF_UP);
    }

    /** Le montant de la commande : ce que l'acheteur voit, TTC, remise comprise. */
    public BigDecimal totalTTC() {
        return lignes.stream().map(this::totalTTCRemise).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String numero() { return numero; }
    public Acheteur acheteur() { return acheteur; }
    public List<LigneCommande> lignes() { return lignes; }
    public int remisePourcent() { return remisePourcent; }
    public LocalDate datePassage() { return datePassage; }
    public ConsignesDeLivraison consignes() { return consignes; }
    public StatutCommande statut() { return statut; }
}
