# Context map : la brûlerie

Domaine d'exemple **fictif** : une brûlerie de café qui vend en ligne à des particuliers et à des entreprises.
Trois contextes bornés, trois langages qui se contredisent sur les mêmes mots.

```
                    ┌──────────────────────────┐
                    │        COMMANDE          │  amont · service hôte ouvert
                    │ acheteur · panier        │  langage publié : commande.api
                    │ montant TTC remisé       │  événement : CommandeValidee
                    └───┬──────────────────┬───┘
     langage publié     │                  │   client-fournisseur
     → conformiste      │                  │   (la Facturation est cliente)
                    ┌───▼────────┐  ┌──────▼──────────────────────┐
                    │ LIVRAISON  │  │ ACL │ FACTURATION           │
                    │destinataire│  │     │ payeur · HT/TVA/TTC   │
                    │colis·créneau│ │     │ échéance · avoir      │
                    └────────────┘  └─────────────────────────────┘
                       chemins séparés entre Livraison et Facturation
```

## Relations

| Amont → aval | Relation | Ce qui traverse | Où c'est dans le code | Règle ArchUnit |
|---|---|---|---|---|
| Commande → (tous) | **Service hôte ouvert + langage publié** : un seul contrat, publié pour tous les consommateurs | l'événement du domaine `CommandeValidee` (abonnement) et sa consultation (`CommandesPubliees`) | `commande.api`, `commande.domaine.Commandes` | `COMMANDE_NE_CONNAIT_PERSONNE` |
| Commande → Livraison | **Conformiste** : la Livraison consomme le langage publié tel quel, sans traduction ; si la Commande le change, elle suit | `CommandeValidee` (consignes de livraison saisies à la commande) | `livraison.domaine.PreparationDepuisCommande` | `LIVRAISON_CONFORMISTE` |
| Commande → Facturation | **Client-fournisseur** : la Commande fournit, la Facturation exprime ses besoins ; côté Facturation, **couche anticorruption** | `CommandeValidee`, traduite : acheteur → payeur, TTC remisé → HT + TVA | `facturation.acl.TraducteurCommande`, `facturation.acl.EmissionDesFactures` | `FACTURATION_PASSE_PAR_L_ACL`, `ACL_NE_LIT_QUE_LE_CONTRAT` |
| Livraison / Facturation | **Chemins séparés** : aucune intégration | rien | — | `LIVRAISON_CONFORMISTE`, `FACTURATION_PASSE_PAR_L_ACL` |

La Commande ne publie que les commandes **engagées** : validées, ou validées avec une annulation en attente (rien n'est encore confirmé). Un brouillon ou une commande annulée n'existe pas pour les autres contextes.

Le câblage des abonnements est fait dans la racine de composition (`demo.JeuDeDonnees`) : la Facturation (via son ACL) et la Livraison s'abonnent à `CommandeValidee`. À l'étape suivante, ce même événement voyagera dans un message A2A.

## Pourquoi une couche anticorruption côté Facturation et pas côté Livraison ?

Ce sont deux choix d'**intégration** face au même langage publié :

- **Couche anticorruption** (Facturation) : son modèle est **contraint de l'extérieur** (mentions légales, TVA, échéance). Elle ne doit pas plier quand la Commande change son panier. Elle traduit, et refuse ce qu'elle ne sait pas traduire (compte sans payeur, catégorie sans taux de TVA).
- **Conformiste** (Livraison) : elle reprend telles quelles les consignes saisies à la commande (destinataire, adresse, créneau). Traduire coûterait plus cher que suivre.

## Les mots qui changent de sens

| Mot courant | Commande | Facturation | Livraison |
|---|---|---|---|
| « client » | **acheteur** : qui passe la commande depuis son compte | **payeur** : entité qui reçoit la facture et la règle | **destinataire** : qui réceptionne le colis |
| « adresse » | aucune (consignes saisies, transmises à la Livraison) | **adresse de facturation** : siège légal du payeur | **adresse de livraison** : point de remise du colis |
| « statut » | brouillon, validée, annulation demandée, annulée | émise, partiellement payée, payée, en retard, annulée par avoir | à préparer, expédié, livré, en échec de présentation, arrêté |
| « montant » | TTC, remise comprise | HT + TVA = TTC ; avoirs ; reste à régler ; trop-perçu | (aucun) |
| « annuler » | demander l'annulation, puis la confirmer ou la refuser | émettre un avoir | arrêter l'acheminement (si le colis n'est pas parti) |
| « échec » | (aucun) | (aucun : on parle de retard) | échec de présentation (destinataire absent…) |

Le mot « client » n'apparaît dans **aucune** description d'outil : un test l'interdit (`CatalogueEtVocabulaireTest`).

## Un sous-agent par contexte

| Sous-agent | Outils (méthodes `@OutilAgent`) | Glossaire |
|---|---|---|
| Commande | `consulterCommande`, `statutCommande`, `demanderAnnulation` | [glossaires/commande.md](glossaires/commande.md) |
| Facturation | `consulterFacture`, `resteARegler`, `listerAvoirs`, `emettreFacture` | [glossaires/facturation.md](glossaires/facturation.md) |
| Livraison | `suivreExpedition`, `modifierAdresseLivraison`, `arreterExpedition` | [glossaires/livraison.md](glossaires/livraison.md) |

L'agent unique, lui, reçoit les 10 outils et les trois langages à la fois. Les deux architectures sont branchées sur Spring AI dans le paquet `ia` (voir le README).
