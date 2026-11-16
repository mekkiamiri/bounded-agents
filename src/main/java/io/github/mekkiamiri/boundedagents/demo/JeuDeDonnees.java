package io.github.mekkiamiri.boundedagents.demo;

import io.github.mekkiamiri.boundedagents.agent.CatalogueOutils;
import io.github.mekkiamiri.boundedagents.commande.domaine.Acheteur;
import io.github.mekkiamiri.boundedagents.commande.domaine.Commande;
import io.github.mekkiamiri.boundedagents.commande.domaine.Commandes;
import io.github.mekkiamiri.boundedagents.commande.domaine.ConsignesDeLivraison;
import io.github.mekkiamiri.boundedagents.commande.domaine.LigneCommande;
import io.github.mekkiamiri.boundedagents.commande.domaine.LigneCommande.Categorie;
import io.github.mekkiamiri.boundedagents.commande.outils.OutilsCommande;
import io.github.mekkiamiri.boundedagents.facturation.acl.EmissionDesFactures;
import io.github.mekkiamiri.boundedagents.facturation.acl.TraducteurCommande;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Facture;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Factures;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Paiement;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Payeur;
import io.github.mekkiamiri.boundedagents.facturation.domaine.Payeurs;
import io.github.mekkiamiri.boundedagents.facturation.outils.OutilsFacturation;
import io.github.mekkiamiri.boundedagents.livraison.domaine.Expedition;
import io.github.mekkiamiri.boundedagents.livraison.domaine.Expeditions;
import io.github.mekkiamiri.boundedagents.livraison.domaine.PreparationDepuisCommande;
import io.github.mekkiamiri.boundedagents.livraison.outils.OutilsLivraison;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Racine de composition : une brûlerie de café fictive qui vend à des particuliers et à des entreprises.
 * Toutes les personnes, entreprises et adresses sont inventées. Date de référence : 10/12/2026.
 *
 * <p>Seul paquet autorisé à connaître les trois contextes (il les assemble, il ne contient aucune règle).
 */
public final class JeuDeDonnees {

    public static final LocalDate DATE_DE_REFERENCE = LocalDate.of(2026, 12, 10);

    private final Commandes commandes = new Commandes();
    private final Payeurs payeurs = new Payeurs();
    private final Factures factures = new Factures();
    private final Expeditions expeditions = new Expeditions();
    private final Clock horloge = Clock.fixed(DATE_DE_REFERENCE.atStartOfDay(ZoneId.of("Europe/Paris")).toInstant(),
            ZoneId.of("Europe/Paris"));
    private final EmissionDesFactures emission = new EmissionDesFactures(commandes, new TraducteurCommande(payeurs), factures);
    private final PreparationDepuisCommande preparation = new PreparationDepuisCommande(commandes, expeditions);

    private JeuDeDonnees() {
    }

    public static JeuDeDonnees brulerie() {
        JeuDeDonnees d = new JeuDeDonnees();
        d.remplir();
        return d;
    }

    private void remplir() {
        payeurs.rattacher("A-17", new Payeur("Studio Pivot SAS", "SIREN 900 000 017 (fictif)", "12 rue des Lilas, 69003 Lyon"));
        payeurs.rattacher("A-23", new Payeur("Lou Garnier", "particulier", "8 impasse du Moulin, 38000 Grenoble"));
        payeurs.rattacher("A-31", new Payeur("Coopérative La Fabrique", "SIREN 900 000 031 (fictif)", "5 place Carnot, 42000 Saint-Étienne"));

        Acheteur ines = new Acheteur("Inès Morel", "A-17");
        Acheteur lou = new Acheteur("Lou Garnier", "A-23");
        Acheteur paul = new Acheteur("Paul Ferrand", "A-31");
        ConsignesDeLivraison accueilPivot = new ConsignesDeLivraison("Karim Benali (accueil)", "4 quai Rambaud, 69002 Lyon", "");

        // C-1033 : particulier, payée, puis avoir partiel.
        valider(new Commande("C-1033", lou, List.of(ligne("Colombie Huila 500 g", Categorie.CAFE, 2, "11.50")), 0,
                LocalDate.of(2026, 10, 20), new ConsignesDeLivraison("Lou Garnier", "8 impasse du Moulin, 38000 Grenoble", "2026-10-23 matin")));
        Facture f1033 = facturer("C-1033", LocalDate.of(2026, 10, 21));
        f1033.encaisser(new Paiement(new BigDecimal("23.00"), LocalDate.of(2026, 10, 21)));
        Expedition e1033 = expedier("C-1033", LocalDate.of(2026, 10, 22));
        e1033.livrer(LocalDate.of(2026, 10, 23));
        f1033.emettreAvoir(new BigDecimal("11.50"), LocalDate.of(2026, 11, 5), "sachet abîmé à la réception");

        // C-1042 : commandée par Inès, payée par Studio Pivot, reçue par l'accueil. Livrée, facture en retard.
        valider(new Commande("C-1042", ines, List.of(ligne("Éthiopie Guji 1 kg", Categorie.CAFE, 3, "24.00")), 10,
                LocalDate.of(2026, 11, 2), avecCreneau(accueilPivot, "2026-11-06 matin")));
        facturer("C-1042", LocalDate.of(2026, 11, 3));
        expedier("C-1042", LocalDate.of(2026, 11, 5)).livrer(LocalDate.of(2026, 11, 6));

        // C-1057 : expédiée, facture partiellement payée.
        valider(new Commande("C-1057", ines, List.of(
                ligne("Moulin manuel acier", Categorie.MATERIEL, 1, "89.00"),
                ligne("Brésil Cerrado 1 kg", Categorie.CAFE, 2, "21.00")), 0,
                LocalDate.of(2026, 12, 1), avecCreneau(accueilPivot, "2026-12-11 après-midi")));
        facturer("C-1057", LocalDate.of(2026, 12, 2))
                .encaisser(new Paiement(new BigDecimal("50.00"), LocalDate.of(2026, 12, 5)));
        expedier("C-1057", LocalDate.of(2026, 12, 9));

        // C-1061 : payée, mais échec de présentation du colis.
        valider(new Commande("C-1061", paul, List.of(ligne("Mélange Fabrique 1 kg", Categorie.CAFE, 4, "19.50")), 0,
                LocalDate.of(2026, 12, 3), new ConsignesDeLivraison("Paul Ferrand", "Atelier 3, 17 rue Neuve, 42000 Saint-Étienne", "2026-12-08 matin")));
        facturer("C-1061", LocalDate.of(2026, 12, 4))
                .encaisser(new Paiement(new BigDecimal("78.00"), LocalDate.of(2026, 12, 4)));
        expedier("C-1061", LocalDate.of(2026, 12, 7))
                .echecDePresentation(LocalDate.of(2026, 12, 8), "destinataire absent", "2026-12-12 matin");

        // C-1070 : panier non validé (brouillon). Ni facture, ni expédition.
        commandes.enregistrer(new Commande("C-1070", lou, List.of(ligne("Kenya AA 250 g", Categorie.CAFE, 1, "9.90")), 0,
                LocalDate.of(2026, 12, 9), new ConsignesDeLivraison("Lou Garnier", "8 impasse du Moulin, 38000 Grenoble", "")));
    }

    private static LigneCommande ligne(String produit, Categorie categorie, int quantite, String prixTTC) {
        return new LigneCommande(produit, categorie, quantite, new BigDecimal(prixTTC));
    }

    private static ConsignesDeLivraison avecCreneau(ConsignesDeLivraison c, String creneau) {
        return new ConsignesDeLivraison(c.nomDestinataire(), c.adresse(), creneau);
    }

    private void valider(Commande c) {
        c.valider();
        commandes.enregistrer(c);
    }

    private Facture facturer(String reference, LocalDate date) {
        return emission.emettre(reference, date).orElseThrow();
    }

    private Expedition expedier(String reference, LocalDate date) {
        Expedition e = preparation.preparer(reference).orElseThrow();
        e.expedier(date);
        return e;
    }

    public OutilsCommande outilsCommande() {
        return new OutilsCommande(commandes);
    }

    public OutilsFacturation outilsFacturation() {
        return new OutilsFacturation(factures, emission, horloge);
    }

    public OutilsLivraison outilsLivraison() {
        return new OutilsLivraison(expeditions);
    }

    public CatalogueOutils catalogue() {
        return CatalogueOutils.depuis(outilsCommande(), outilsFacturation(), outilsLivraison());
    }

    public Commandes commandes() { return commandes; }
    public Payeurs payeurs() { return payeurs; }
    public Factures factures() { return factures; }
    public Expeditions expeditions() { return expeditions; }
}
