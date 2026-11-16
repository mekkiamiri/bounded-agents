// Généré par DonneesPage.java depuis le catalogue d'outils, glossaires/ et scenarios/ : ne pas modifier à la main.
window.DONNEES = {
  "outils" : [ {
    "contexte" : "commande",
    "nom" : "consulterCommande",
    "description" : "Donne l'acheteur, les lignes du panier, la remise et le montant TTC d'une commande."
  }, {
    "contexte" : "commande",
    "nom" : "demanderAnnulation",
    "description" : "Enregistre une demande d'annulation de la commande par l'acheteur. Un brouillon est annulé tout de suite ; une commande validée reste à confirmer."
  }, {
    "contexte" : "commande",
    "nom" : "statutCommande",
    "description" : "Donne le statut de la commande : brouillon, validée, annulation demandée ou annulée. Ne dit rien du paiement ni de l'acheminement."
  }, {
    "contexte" : "facturation",
    "nom" : "consulterFacture",
    "description" : "Donne la facture liée à une référence de commande : payeur, adresse de facturation, montants HT, TVA et TTC, échéance et statut de paiement."
  }, {
    "contexte" : "facturation",
    "nom" : "emettreFacture",
    "description" : "Émet la facture d'une commande validée (une seule par référence) et renvoie son numéro."
  }, {
    "contexte" : "facturation",
    "nom" : "listerAvoirs",
    "description" : "Liste les avoirs émis sur la facture d'une référence de commande (montant TTC, date, motif)."
  }, {
    "contexte" : "facturation",
    "nom" : "resteARegler",
    "description" : "Donne le montant TTC qui reste à régler par le payeur sur la facture d'une référence de commande."
  }, {
    "contexte" : "livraison",
    "nom" : "arreterExpedition",
    "description" : "Arrête l'acheminement du colis s'il n'est pas encore parti (préalable à une annulation)."
  }, {
    "contexte" : "livraison",
    "nom" : "modifierAdresseLivraison",
    "description" : "Change l'adresse de livraison du colis tant qu'il n'est pas parti."
  }, {
    "contexte" : "livraison",
    "nom" : "suivreExpedition",
    "description" : "Donne l'état d'acheminement du colis d'une référence de commande : destinataire, adresse de livraison, créneau, statut, date du dernier événement, motif d'échec."
  } ],
  "glossaires" : {
    "commande" : [ [ "Acheteur", "Personne qui passe la commande depuis son compte (A-17)." ], [ "Compte", "Identifiant de l'acheteur. Seul identifiant partagé avec la Facturation, qui en déduit le payeur." ], [ "Panier", "Lignes choisies par l'acheteur avant validation." ], [ "Ligne", "Produit, catégorie (CAFE, MATERIEL), quantité, prix unitaire TTC affiché." ], [ "Remise", "Pourcentage appliqué à tout le panier." ], [ "Montant", "Total TTC remise comprise, arrondi au centime par ligne." ], [ "Brouillon", "Panier non validé. N'existe pas pour les autres contextes." ], [ "Validée", "L'acheteur a confirmé. L'événement CommandeValidee est publié aux autres contextes." ], [ "Annulation demandée", "L'acheteur veut annuler une commande validée. La commande reste publiée jusqu'à la décision : confirmée (annulée) ou refusée (de nouveau validée, par exemple si le colis est parti)." ], [ "Consignes de livraison", "Destinataire, adresse et créneau saisis à la commande ; la Livraison en devient propriétaire." ] ],
    "facturation" : [ [ "Payeur", "Entité qui reçoit la facture et la règle : raison sociale, identifiant fiscal, adresse de facturation. Rattaché à un compte." ], [ "Adresse de facturation", "Adresse légale du payeur, portée sur la facture." ], [ "Facture", "Une par commande validée, numéro F-<n°>, référence de la commande." ], [ "Montant HT / TVA / TTC", "HT arrondi au centime par ligne ; TVA = TTC − HT ; taux réduit 5,5 % (café), normal 20 % (matériel)." ], [ "Échéance", "Date d'émission + 30 jours." ], [ "Reste à régler", "TTC − avoirs − paiements reçus (jamais négatif)." ], [ "Trop-perçu", "Paiements reçus au-delà de TTC − avoirs : à rembourser au payeur." ], [ "Statut", "émise, partiellement payée, payée, en retard (échéance dépassée et reste à régler non nul), annulée par avoir." ], [ "Avoir", "Document qui annule tout ou partie d'une facture (AV-<n°>-<rang>). Strictement positif, plafonné à ce qui n'est pas déjà annulé. Ce que le langage courant appelle « remboursement »." ] ],
    "livraison" : [ [ "Destinataire", "Personne qui réceptionne le colis (par exemple l'accueil d'un bureau)." ], [ "Expédition", "Acheminement d'une commande validée jusqu'au destinataire." ], [ "Colis", "Ce qui est acheminé." ], [ "Adresse de livraison", "Point de remise. Modifiable tant que le colis n'est pas parti." ], [ "Créneau", "Moment de présentation prévu." ], [ "Statut", "à préparer, expédié, livré, en échec de présentation, arrêté." ], [ "Échec de présentation", "Le colis n'a pas pu être remis (motif : destinataire absent…) ; nouveau créneau fixé. Peut se répéter ; le retour à l'expéditeur n'est pas modélisé en v0." ], [ "Arrêter l'acheminement", "Seulement avant le départ du colis. Après : retour." ] ]
  },
  "jeu" : {
    "version" : "v0 · 16/11/2026",
    "fuites_globales" : [ "client", "clients" ],
    "scenarios" : [ {
      "id" : "S01",
      "question" : "La commande C-1042 est-elle terminée ?",
      "piege" : "« Terminée » n'existe dans aucun contexte. La Commande dit « validée », la Livraison « livrée », la Facturation « en retard » : un agent qui ne regarde qu'un statut répond « oui ».",
      "contextes" : [ "commande", "livraison", "facturation" ],
      "reponse_attendue" : "Pas entièrement. Côté Commande, C-1042 est validée. Côté Livraison, le colis a été livré le 2026-11-06 à Karim Benali (accueil). Côté Facturation, la facture F-1042 est en retard : 64,80 € TTC restent à régler, échéance dépassée depuis le 2026-12-03.",
      "doit_contenir" : [ [ "livré", "livrée" ], [ "en retard" ], [ "64,80" ] ],
      "ne_doit_pas_contenir" : [ "oui, elle est terminée", "commande terminée", "entièrement terminée" ],
      "fuites" : [ "statut terminé" ],
      "verifications" : [ {
        "outil" : "statutCommande",
        "arguments" : [ "C-1042" ],
        "contient" : "commande validée"
      }, {
        "outil" : "suivreExpedition",
        "arguments" : [ "C-1042" ],
        "contient" : "colis livré le 2026-11-06"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1042" ],
        "contient" : "facture en retard"
      } ]
    }, {
      "id" : "S02",
      "question" : "Quelle est l'adresse du client de la commande C-1042 ?",
      "piege" : "« Adresse » a deux sens : adresse de livraison (Livraison, l'accueil d'un bureau) et adresse de facturation (Facturation, le siège du payeur). Ici elles diffèrent.",
      "contextes" : [ "livraison", "facturation" ],
      "reponse_attendue" : "Il y a deux adresses différentes : l'adresse de livraison est 4 quai Rambaud, 69002 Lyon (accueil, destinataire Karim Benali) ; l'adresse de facturation du payeur Studio Pivot SAS est 12 rue des Lilas, 69003 Lyon.",
      "doit_contenir" : [ [ "quai rambaud" ], [ "rue des lilas" ] ],
      "ne_doit_pas_contenir" : [ ],
      "fuites" : [ "adresse du client" ],
      "verifications" : [ {
        "outil" : "suivreExpedition",
        "arguments" : [ "C-1042" ],
        "contient" : "adresse de livraison : 4 quai Rambaud, 69002 Lyon"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1042" ],
        "contient" : "adresse de facturation : 12 rue des Lilas, 69003 Lyon"
      } ]
    }, {
      "id" : "S03",
      "question" : "Qui est le client de la commande C-1042 ?",
      "piege" : "« Client » a trois sens : l'acheteur (Commande), le payeur (Facturation), le destinataire (Livraison). Ce sont trois personnes différentes.",
      "contextes" : [ "commande", "facturation", "livraison" ],
      "reponse_attendue" : "Trois rôles, trois personnes : l'acheteur est Inès Morel (compte A-17), le payeur est Studio Pivot SAS, le destinataire est Karim Benali à l'accueil.",
      "doit_contenir" : [ [ "inès morel" ], [ "studio pivot" ], [ "karim benali" ] ],
      "ne_doit_pas_contenir" : [ ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "consulterCommande",
        "arguments" : [ "C-1042" ],
        "contient" : "par l'acheteur Inès Morel (compte A-17)"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1042" ],
        "contient" : "Payeur : Studio Pivot SAS"
      }, {
        "outil" : "suivreExpedition",
        "arguments" : [ "C-1042" ],
        "contient" : "Destinataire : Karim Benali (accueil)"
      } ]
    }, {
      "id" : "S04",
      "question" : "Combien reste-t-il à payer sur la commande C-1057 ?",
      "piege" : "Le « montant » de la Commande (131,00 € TTC) n'est pas ce qui reste dû ; la Facturation parle aussi en HT (113,98 €).",
      "contextes" : [ "facturation" ],
      "reponse_attendue" : "81,00 € TTC restent à régler sur la facture F-1057 (131,00 € TTC, 50,00 € déjà réglés), échéance 2027-01-01.",
      "doit_contenir" : [ [ "81,00" ] ],
      "ne_doit_pas_contenir" : [ "113,98", "131,00 € restent", "reste 131,00" ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "resteARegler",
        "arguments" : [ "C-1057" ],
        "contient" : "reste à régler 81,00 € TTC sur 131,00 € TTC"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1057" ],
        "contient" : "113,98 € HT"
      } ]
    }, {
      "id" : "S05",
      "question" : "Peut-on annuler la commande C-1057 ?",
      "piege" : "La Commande accepte une demande d'annulation, mais seule la Livraison sait que le colis est parti. Un agent qui appelle seulement demanderAnnulation annonce une annulation qui n'aura pas lieu.",
      "contextes" : [ "commande", "livraison" ],
      "reponse_attendue" : "Pas directement : le colis est expédié depuis le 2026-12-09, son acheminement ne peut plus être arrêté. La Commande peut enregistrer une demande d'annulation, qui se traitera comme un retour à réception ; la Facturation émettra ensuite un avoir.",
      "doit_contenir" : [ [ "expédié", "expédiée", "parti" ], [ "retour" ] ],
      "ne_doit_pas_contenir" : [ "commande annulée", "annulation confirmée", "oui, elle est annulée" ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "arreterExpedition",
        "arguments" : [ "C-1057" ],
        "contient" : "arrêt impossible, colis expédié le 2026-12-09"
      }, {
        "outil" : "demanderAnnulation",
        "arguments" : [ "C-1057" ],
        "contient" : "annulation demandée ; à confirmer"
      } ]
    }, {
      "id" : "S06",
      "question" : "Pourquoi la commande C-1061 est-elle en échec ?",
      "piege" : "« Échec » n'existe que côté Livraison (échec de présentation). Un agent qui mélange pense à un paiement refusé : la facture est pourtant payée.",
      "contextes" : [ "livraison", "facturation" ],
      "reponse_attendue" : "Ce n'est pas la commande qui est en échec mais la présentation du colis : le 2026-12-08, le destinataire Paul Ferrand était absent. Nouvelle présentation prévue le 2026-12-12 matin. La facture F-1061 est payée.",
      "doit_contenir" : [ [ "absent" ], [ "2026-12-12" ] ],
      "ne_doit_pas_contenir" : [ "paiement refusé", "impayé", "rejet de paiement" ],
      "fuites" : [ "commande en échec" ],
      "verifications" : [ {
        "outil" : "suivreExpedition",
        "arguments" : [ "C-1061" ],
        "contient" : "colis en échec de présentation le 2026-12-08 (motif : destinataire absent)"
      }, {
        "outil" : "suivreExpedition",
        "arguments" : [ "C-1061" ],
        "contient" : "Créneau de présentation : 2026-12-12 matin"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1061" ],
        "contient" : "facture payée"
      } ]
    }, {
      "id" : "S07",
      "question" : "Change l'adresse de la commande C-1057 pour 9 rue Paul Bert, 69003 Lyon.",
      "piege" : "L'adresse de livraison ne change plus après expédition. Changer l'adresse de facturation du payeur à la place serait une fuite grave.",
      "contextes" : [ "livraison" ],
      "reponse_attendue" : "Impossible pour l'adresse de livraison : le colis est expédié depuis le 2026-12-09. Si c'est l'adresse de facturation de Studio Pivot SAS qu'il faut changer, c'est une autre demande, à traiter par la Facturation.",
      "doit_contenir" : [ [ "impossible", "ne peut plus" ], [ "expédié", "expédiée" ] ],
      "ne_doit_pas_contenir" : [ "adresse modifiée", "adresse changée", "adresse mise à jour" ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "modifierAdresseLivraison",
        "arguments" : [ "C-1057", "9 rue Paul Bert, 69003 Lyon" ],
        "contient" : "modification impossible, colis expédié le 2026-12-09"
      } ]
    }, {
      "id" : "S08",
      "question" : "La commande C-1033 a-t-elle été remboursée ?",
      "piege" : "« Remboursée » n'existe pas : la Facturation parle d'avoir, ici partiel (11,50 € sur 23,00 €).",
      "contextes" : [ "facturation" ],
      "reponse_attendue" : "En partie : la facture F-1033 (23,00 € TTC) est payée, et un avoir AV-1033-1 de 11,50 € TTC a été émis le 2026-11-05 (sachet abîmé à la réception) ; ces 11,50 € sont un trop-perçu à rembourser au payeur, Lou Garnier.",
      "doit_contenir" : [ [ "11,50" ], [ "avoir" ] ],
      "ne_doit_pas_contenir" : [ "23,00 € remboursés", "remboursée en totalité", "intégralement remboursée" ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "listerAvoirs",
        "arguments" : [ "C-1033" ],
        "contient" : "avoir AV-1033-1 de 11,50 € TTC émis le 2026-11-05"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1033" ],
        "contient" : "facture payée"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1033" ],
        "contient" : "Trop-perçu à rembourser au payeur : 11,50 €"
      } ]
    }, {
      "id" : "S09",
      "question" : "Quand la commande C-1070 sera-t-elle livrée ?",
      "piege" : "C-1070 est un brouillon : la Commande ne l'a pas publiée, la Livraison n'en a jamais entendu parler. Un agent inventif propose une date.",
      "contextes" : [ "commande", "livraison" ],
      "reponse_attendue" : "Pas pour l'instant : C-1070 est un brouillon que l'acheteuse Lou Garnier n'a pas validé. Aucune expédition n'existe tant que la commande n'est pas validée.",
      "doit_contenir" : [ [ "brouillon", "pas validée", "non validée" ] ],
      "ne_doit_pas_contenir" : [ "sera livrée le", "livraison prévue le" ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "statutCommande",
        "arguments" : [ "C-1070" ],
        "contient" : "commande brouillon"
      }, {
        "outil" : "suivreExpedition",
        "arguments" : [ "C-1070" ],
        "contient" : "aucune expédition"
      } ]
    }, {
      "id" : "S10",
      "question" : "Quel est le statut de la commande C-1057 ?",
      "piege" : "Trois statuts coexistent. Répondre par un seul, c'est choisir un contexte sans le dire.",
      "contextes" : [ "commande", "livraison", "facturation" ],
      "reponse_attendue" : "Trois statuts, un par contexte : commande validée ; colis expédié le 2026-12-09, présentation prévue le 2026-12-11 après-midi ; facture partiellement payée, 81,00 € TTC restent à régler.",
      "doit_contenir" : [ [ "validée" ], [ "expédié", "expédiée" ], [ "partiellement payée" ] ],
      "ne_doit_pas_contenir" : [ ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "statutCommande",
        "arguments" : [ "C-1057" ],
        "contient" : "commande validée"
      }, {
        "outil" : "suivreExpedition",
        "arguments" : [ "C-1057" ],
        "contient" : "colis expédié le 2026-12-09"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1057" ],
        "contient" : "facture partiellement payée"
      } ]
    }, {
      "id" : "S11",
      "question" : "Émets la facture de la commande C-1070.",
      "piege" : "Un brouillon ne passe pas la couche anticorruption : la Facturation ne voit que les commandes validées.",
      "contextes" : [ "facturation", "commande" ],
      "reponse_attendue" : "Impossible : C-1070 n'est pas validée (c'est un brouillon), il n'y a rien à facturer.",
      "doit_contenir" : [ [ "brouillon", "pas validée", "non validée", "aucune commande validée" ], [ "rien à facturer", "impossible" ] ],
      "ne_doit_pas_contenir" : [ "facture émise" ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "emettreFacture",
        "arguments" : [ "C-1070" ],
        "contient" : "aucune commande validée à cette référence, rien à facturer"
      }, {
        "outil" : "statutCommande",
        "arguments" : [ "C-1070" ],
        "contient" : "commande brouillon"
      } ]
    }, {
      "id" : "S12",
      "question" : "Quel est le montant de la commande C-1042 ?",
      "piege" : "La Commande parle TTC remise comprise (64,80 €) ; la Facturation détaille HT et TVA (61,42 € + 3,38 €). Le prix avant remise (72,00 €) est faux.",
      "contextes" : [ "commande", "facturation" ],
      "reponse_attendue" : "64,80 € TTC, remise de 10 % comprise (3 x Éthiopie Guji 1 kg à 24,00 €). Sur la facture F-1042, cela fait 61,42 € HT et 3,38 € de TVA.",
      "doit_contenir" : [ [ "64,80" ] ],
      "ne_doit_pas_contenir" : [ "72,00 € ttc", "montant de 72,00", "montant : 72,00" ],
      "fuites" : [ ],
      "verifications" : [ {
        "outil" : "consulterCommande",
        "arguments" : [ "C-1042" ],
        "contient" : "Montant : 64,80 € TTC remise comprise"
      }, {
        "outil" : "consulterFacture",
        "arguments" : [ "C-1042" ],
        "contient" : "61,42 € HT + 3,38 € de TVA = 64,80 € TTC"
      } ]
    } ]
  }
};
