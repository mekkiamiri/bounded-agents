# Langage omniprésent : Commande

| Terme | Définition | À ne pas confondre avec |
|---|---|---|
| Acheteur | Personne qui passe la commande depuis son **compte** (`A-17`). | payeur (Facturation), destinataire (Livraison) |
| Compte | Identifiant de l'acheteur. Seul identifiant partagé avec la Facturation, qui en déduit le payeur. | — |
| Panier | Lignes choisies par l'acheteur avant validation. | facture |
| Ligne | Produit, catégorie (`CAFE`, `MATERIEL`), quantité, prix unitaire TTC affiché. | ligne de facture (HT) |
| Remise | Pourcentage appliqué à tout le panier. | avoir |
| Montant | Total **TTC remise comprise**, arrondi au centime par ligne. | montant HT, reste à régler |
| Brouillon | Panier non validé. N'existe pas pour les autres contextes. | — |
| Validée | L'acheteur a confirmé. L'événement `CommandeValidee` est publié aux autres contextes. | payée, livrée |
| Annulation demandée | L'acheteur veut annuler une commande validée. La commande reste publiée jusqu'à la décision : confirmée (annulée) ou refusée (de nouveau validée, par exemple si le colis est parti). | annulée, avoir |
| Consignes de livraison | Destinataire, adresse et créneau saisis à la commande ; la Livraison en devient propriétaire. | adresse de facturation |

Outils : `consulterCommande`, `statutCommande`, `demanderAnnulation` (`commande.outils.OutilsCommande`).
