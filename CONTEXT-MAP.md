# Context map : la brûlerie

Domaine d'exemple **fictif** : une brûlerie de café qui vend en ligne à des particuliers et à des entreprises.
Trois contextes bornés, trois langages qui se contredisent sur les mêmes mots.

```
                    ┌──────────────────────┐
                    │      COMMANDE        │  amont
                    │ acheteur · panier    │  publie : commande.api.CommandeValidee
                    │ montant TTC remisé   │
                    └───┬──────────────┬───┘
         partenariat    │              │   client-fournisseur
   (contrat co-construit)│              │   (la Facturation est cliente)
                    ┌───▼───────┐  ┌───▼──────────────────────────┐
                    │ LIVRAISON │  │ ACL │ FACTURATION            │
                    │destinataire│  │     │ payeur · HT/TVA/TTC    │
                    │colis·créneau│ │     │ échéance · avoir       │
                    └────────────┘  └──────────────────────────────┘
                     aucune relation entre Livraison et Facturation
```

## Relations

| Amont → aval | Relation | Ce qui traverse | Où c'est dans le code | Règle ArchUnit |
|---|---|---|---|---|
| Commande → Livraison | **Partenariat** : les deux équipes font évoluer ensemble le contrat publié | `CommandeValidee` (consignes de livraison saisies à la commande) | `livraison.domaine.PreparationDepuisCommande` | `LIVRAISON_LIT_SEULEMENT_LE_CONTRAT` |
| Commande → Facturation | **Client-fournisseur** : la Commande fournit, la Facturation exprime ses besoins ; côté Facturation, **couche anticorruption** | `CommandeValidee`, traduite : acheteur → payeur, TTC remisé → HT + TVA | `facturation.acl.TraducteurCommande`, `facturation.acl.EmissionDesFactures` | `FACTURATION_PASSE_PAR_L_ACL`, `ACL_NE_LIT_QUE_LE_CONTRAT` |
| Livraison ↔ Facturation | **Aucune** (chemins séparés) | rien | — | `LIVRAISON_LIT_SEULEMENT_LE_CONTRAT`, `FACTURATION_PASSE_PAR_L_ACL` |

Pour toutes les relations : la Commande ne publie que les commandes **validées** (un brouillon n'existe pas pour les autres contextes).

## Pourquoi une couche anticorruption côté Facturation et pas côté Livraison ?

- La Facturation a un modèle **contraint de l'extérieur** (mentions légales, TVA, échéance) : elle ne doit pas plier quand la Commande change son panier. Elle traduit, et refuse ce qu'elle ne sait pas traduire (compte sans payeur, catégorie sans taux de TVA).
- La Livraison et la Commande partagent la même saisie (destinataire, adresse, créneau) et évoluent ensemble : un partenariat coûte moins cher qu'une traduction.

## Les mots qui changent de sens

| Mot courant | Commande | Facturation | Livraison |
|---|---|---|---|
| « client » | **acheteur** : qui passe la commande depuis son compte | **payeur** : entité qui reçoit la facture et la règle | **destinataire** : qui réceptionne le colis |
| « adresse » | aucune (consignes saisies, transmises à la Livraison) | **adresse de facturation** : siège légal du payeur | **adresse de livraison** : point de remise du colis |
| « statut » | brouillon, validée, annulation demandée, annulée | émise, partiellement payée, payée, en retard | à préparer, expédié, livré, en échec de présentation, arrêté |
| « montant » | TTC, remise comprise | HT + TVA = TTC ; reste à régler | (aucun) |
| « annuler » | demander l'annulation | émettre un avoir | arrêter l'acheminement (si le colis n'est pas parti) |
| « échec » | (aucun) | (aucun : on parle de retard) | échec de présentation (destinataire absent…) |

Le mot « client » n'apparaît dans **aucune** description d'outil : un test l'interdit (`CatalogueEtVocabulaireTest`).

## Un sous-agent par contexte

| Sous-agent | Outils (méthodes `@OutilAgent`) | Glossaire |
|---|---|---|
| Commande | `consulterCommande`, `statutCommande`, `demanderAnnulation` | [glossaires/commande.md](glossaires/commande.md) |
| Facturation | `consulterFacture`, `resteARegler`, `listerAvoirs`, `emettreFacture` | [glossaires/facturation.md](glossaires/facturation.md) |
| Livraison | `suivreExpedition`, `modifierAdresseLivraison`, `arreterExpedition` | [glossaires/livraison.md](glossaires/livraison.md) |

L'agent unique, lui, reçoit les 10 outils et les trois langages à la fois.
