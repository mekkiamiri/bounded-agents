# Langage omniprésent : Livraison

| Terme | Définition | À ne pas confondre avec |
|---|---|---|
| Destinataire | Personne qui réceptionne le colis (par exemple l'accueil d'un bureau). | acheteur, payeur |
| Expédition | Acheminement d'une commande validée jusqu'au destinataire. | commande |
| Colis | Ce qui est acheminé. | panier, ligne |
| Adresse de livraison | Point de remise. Modifiable tant que le colis n'est pas parti. | adresse de facturation |
| Créneau | Moment de présentation prévu. | échéance |
| Statut | à préparer, expédié, livré, en échec de présentation, arrêté. | statut de commande, statut de facture |
| Échec de présentation | Le colis n'a pas pu être remis (motif : destinataire absent…) ; nouveau créneau fixé. Peut se répéter ; le retour à l'expéditeur n'est pas modélisé en v0. | retard de paiement |
| Arrêter l'acheminement | Seulement avant le départ du colis. Après : retour. | annuler (Commande), avoir (Facturation) |

Conformiste au langage publié de la Commande : `livraison.domaine.PreparationDepuisCommande` consomme l'événement `commande.api.CommandeValidee` tel quel.
Outils : `suivreExpedition`, `modifierAdresseLivraison`, `arreterExpedition` (`livraison.outils.OutilsLivraison`).
