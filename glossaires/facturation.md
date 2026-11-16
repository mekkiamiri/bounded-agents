# Langage omniprésent : Facturation

| Terme | Définition | À ne pas confondre avec |
|---|---|---|
| Payeur | Entité qui reçoit la facture et la règle : raison sociale, identifiant fiscal, adresse de facturation. Rattaché à un compte. | acheteur, destinataire |
| Adresse de facturation | Adresse légale du payeur, portée sur la facture. | adresse de livraison |
| Facture | Une par commande validée, numéro `F-<n°>`, référence de la commande. | commande |
| Montant HT / TVA / TTC | HT arrondi au centime par ligne ; TVA = TTC − HT ; taux réduit 5,5 % (café), normal 20 % (matériel). | montant de la Commande (TTC remisé) |
| Échéance | Date d'émission + 30 jours. | créneau |
| Reste à régler | TTC − paiements reçus. | montant |
| Statut | émise, partiellement payée, payée, en retard (échéance dépassée et reste à régler non nul). | statut de commande, statut de livraison |
| Avoir | Document qui annule tout ou partie d'une facture (`AV-<n°>-<rang>`). Ce que le langage courant appelle « remboursement ». | remise, annulation |

Couche anticorruption : `facturation.acl.TraducteurCommande` (acheteur → payeur, TTC → HT + TVA).
Outils : `consulterFacture`, `resteARegler`, `listerAvoirs`, `emettreFacture` (`facturation.outils.OutilsFacturation`).
