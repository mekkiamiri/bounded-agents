# Scénarios métier

`scenarios.json` contient 12 questions posées **en langage courant** (avec « client », « adresse », « statut »…),
comme les poserait un utilisateur. Chacune a :

- `piege` : le mot qui change de sens d'un contexte à l'autre ;
- `contextes` : les sous-agents qui doivent répondre ;
- `reponse_attendue` : la réponse de référence, écrite dans les langages des contextes ;
- `doit_contenir` : groupes de fragments (chaque groupe doit apparaître, au moins une variante) ;
- `ne_doit_pas_contenir` : erreurs factuelles qui font échouer la réponse ;
- `fuites` (+ `fuites_globales`, dont le mot « client ») : expressions comptées comme **erreurs de vocabulaire** ;
- `verifications` : appels d'outils qui prouvent la réponse sur le jeu de données (rejoués par `ScenariosTest`).

La notation (`scenarios.Notation`) est déterministe : minuscules, sans accents, correspondance de fragments.
Elle ne remplace pas une relecture humaine des échecs, prévue au protocole.

## Protocole de comparaison (étape suivante, rien n'est encore mesuré)

| Mesure | Agent unique (10 outils) | Agents découpés (routeur + 3 sous-agents) |
|---|---|---|
| Taux de réussite (12 scénarios × 5 passages) | à mesurer | à mesurer |
| Tokens (entrée + sortie, par scénario) | à mesurer | à mesurer |
| Erreurs de vocabulaire (fuites comptées) | à mesurer | à mesurer |
| Appels d'outils inutiles ou dans le mauvais contexte | à mesurer | à mesurer |

Mêmes modèle, température, outils et données pour les deux architectures ; seul le découpage change.
Les réponses brutes seront versionnées dans `results/` avec le modèle et la date.
