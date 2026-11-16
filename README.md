# bounded-agents

> **Un sous-agent par bounded context** : découper ses agents IA comme on découpe un domaine, avec un context map, un langage omniprésent par contexte et une couche anticorruption, puis mesurer si ça marche mieux qu'un agent unique.

Dépôt de démo du talk « Un sous-agent par bounded context : le DDD pour vos agents » (Mekki Amiri, proposé à Devoxx France 2027).

- Page de démo (context map interactif) : https://mekkiamiri.github.io/bounded-agents/
- Slides : https://mekkiamiri.github.io/bounded-agents/slides/

## Ce qui tourne en v0

Un domaine d'exemple **fictif** (une brûlerie de café qui vend à des particuliers et à des entreprises), en Java 21 :

- **3 contextes bornés** dont les vocabulaires se contredisent : *Commande* (acheteur, montant TTC remisé), *Facturation* (payeur, HT/TVA, avoir), *Livraison* (destinataire, colis, adresse de livraison). « Client », « adresse », « statut », « montant » n'y ont pas le même sens : voir [CONTEXT-MAP.md](CONTEXT-MAP.md) et les [glossaires](glossaires/).
- **Un context map dans le code** : la Commande expose un service hôte ouvert et publie l'événement du domaine `CommandeValidee` dans son langage publié (`commande.api`) ; la Livraison est **conformiste** (elle le consomme tel quel) ; la Facturation est cliente (client-fournisseur) et le traduit dans sa **couche anticorruption** (`facturation.acl` : acheteur → payeur, TTC remisé → HT + TVA) ; Livraison et Facturation suivent des **chemins séparés**.
- **Les frontières prouvées par des tests ArchUnit** : un outil ne manipule que le modèle de son contexte, la Livraison ne dépend que de `commande.api`, seule la couche anticorruption de la Facturation voit la Commande, le domaine ignore les agents, Spring AI reste dans l'adaptateur `ia`, pas de cycle. Un test vérifie que les règles **attrapent** une vraie fuite (`OutilFuyant`, dans les sources de test).
- **Les outils de chaque sous-agent** : méthodes annotées `@OutilAgent` dans `<contexte>.outils` (3 + 4 + 3 = 10 outils), décrites dans le langage de leur contexte ; un test interdit les mots des autres contextes et le mot « client ».
- **Les deux architectures branchées sur Spring AI 2.0.2** (paquet `ia`) : l'agent unique (un `ChatClient`, 10 outils, trois glossaires) et l'agent découpé (un routeur qui consulte trois sous-agents, chacun avec son glossaire et ses seuls outils). En CI, aucun modèle n'est appelé : un faux `ChatModel` scénarisé demande des outils et Spring AI les exécute sur le vrai domaine (`ArchitecturesTest`).
- **12 scénarios métier** ([scenarios/](scenarios/)) avec réponse attendue, pièges de vocabulaire et vérifications rejouées sur un jeu de données neuf, plus une notation déterministe (mot entier, négations, erreurs factuelles, fuites de vocabulaire).

## Lancer

```bash
mvn -B verify
```

Java 21 et Maven. Résultat attendu : 67 tests verts (domaine, couche anticorruption, ArchUnit, vocabulaire, Spring AI avec faux modèle, scénarios, données de la page). La CI GitHub Actions lance la même commande.

Démo avec un vrai modèle (S01 à S03, agent unique puis agents découpés ; une passe, ce n'est pas une mesure) :

```bash
BA_MODELE=<modèle> BA_CLE_API=<clé> mvn -q compile exec:java
# API compatible OpenAI (OpenAI, serveur local…) : ajouter BA_URL_BASE=<url>
```

La page lit `docs/donnees.js`, généré depuis le catalogue d'outils, les glossaires et les scénarios ; le test `PageCoherenceTest` échoue s'il est périmé. Régénérer : `mvn -B test -Dtest=PageCoherenceTest -DregenererPage=true`.

## État (honnête)

| Élément | État |
|---|---|
| Domaine, 3 contextes, context map, glossaires | fait, testé |
| Événement du domaine `CommandeValidee`, abonnements Facturation et Livraison | fait, testé |
| Outils par contexte (`@OutilAgent`) | fait, testé |
| Couche anticorruption Commande → Facturation | fait, testée |
| Frontières ArchUnit (+ test qui prouve qu'elles attrapent une fuite) | fait |
| 12 scénarios + notation | fait ; réponses de référence vérifiées par les outils |
| Agent unique et agents découpés avec Spring AI 2.0.2 | fait, câblage testé avec un faux modèle ; démo S01 à S03 avec un vrai modèle via `exec:java` |
| Sous-agents qui se parlent en A2A (aujourd'hui : appel local) | **étape suivante** |
| Interface AG-UI (les deux architectures côte à côte) | **étape suivante** |
| Taux de réussite, tokens, erreurs de vocabulaire : agent unique contre agents découpés | **à mesurer** (aucun chiffre publié) |

Seule mesure disponible aujourd'hui, statique et sans modèle : l'agent unique doit départager **10 outils** dans trois langages ; chaque sous-agent en voit **au plus 4**, dans un seul langage (vérifié par `CatalogueEtVocabulaireTest` et `ArchitecturesTest`). Ce que ça change sur la qualité des réponses reste **à mesurer**.

## Étape suivante : A2A et AG-UI

Dans le `pom.xml` : `org.springframework.ai:spring-ai-bom` 2.0.2 (`spring-ai-client-chat`, `spring-ai-openai`). Prévu ensuite (versions vérifiées sur Maven Central le 16/11/2026) :

| Brique | Artefact | Version | Rôle |
|---|---|---|---|
| Spring Boot | `org.springframework.boot:spring-boot-starter-parent` | 4.1.2 | socle des serveurs d'agents |
| Spring AI A2A | `org.springaicommunity:spring-ai-a2a-server-autoconfigure` | 0.4.0 | chaque sous-agent publié comme serveur A2A (carte d'agent, messages) |
| SDK A2A Java | `org.a2aproject.sdk:a2a-java-sdk-*` | 1.4.0.Final (tiré par Spring AI A2A 0.4.0 ; dernière : 1.5.1.Final) | protocole A2A |
| AG-UI | `com.ag-ui.community:ag-ui-spring-ai-webmvc-boot-starter` | 2.0.0 | interface qui pilote les deux architectures |

Points à vérifier en codant : Spring AI A2A 0.4.0 est compilé contre Spring AI 2.0.1 / Spring Boot 4.1.1 et AG-UI 2.0.0 contre Spring AI 2.0.0 ; l'alignement sur 2.0.2 / 4.1.2 devra être testé.

Ce qui sera construit :

1. **A2A** : le routeur appelle aujourd'hui chaque sous-agent en local (`SousAgentSpringAi`) ; chaque sous-agent deviendra un serveur A2A, et `CommandeValidee` voyagera dans un message.
2. **Banc** : les 12 scénarios × 5 passages sur les deux architectures, mêmes modèle et température ; réponses brutes et tokens versionnés dans `results/`.
3. **AG-UI** : la même question posée aux deux architectures, côte à côte.

## Structure

```
src/main/java/io/github/mekkiamiri/boundedagents/
  agent/          @OutilAgent, catalogue, configurations (agent unique / découpés)
  ia/             adaptateur Spring AI : outils → ToolCallback, agent unique, routeur + sous-agents, démo
  commande/       domaine/, api/ (langage publié, événement CommandeValidee), outils/
  facturation/    domaine/, acl/ (couche anticorruption), outils/
  livraison/      domaine/ (conformiste à commande.api), outils/
  demo/           jeu de données fictif (racine de composition), données de la page
  scenarios/      lecture et notation des scénarios
scenarios/        scenarios.json + protocole
glossaires/       langage omniprésent de chaque contexte
docs/             page de démo (index.html + donnees.js généré) et slides (GitHub Pages)
slides/           source des slides
```

## Licence

Apache-2.0, voir [LICENSE](LICENSE). Personnes, entreprises et adresses du jeu de données : toutes fictives.
