# bounded-agents

> **Un sous-agent par bounded context** : découper ses agents IA comme on découpe un domaine, avec un context map, un langage omniprésent par contexte et une couche anticorruption, puis mesurer si ça marche mieux qu'un agent unique.

Dépôt de démo du talk « Un sous-agent par bounded context : le DDD pour vos agents » (Mekki Amiri, proposé à Devoxx France 2027).

- Page de démo (context map interactif) : https://mekkiamiri.github.io/bounded-agents/
- Slides : https://mekkiamiri.github.io/bounded-agents/slides/

## Ce qui tourne en v0

Un domaine d'exemple **fictif** (une brûlerie de café qui vend à des particuliers et à des entreprises), en Java 21, sans framework :

- **3 contextes bornés** dont les vocabulaires se contredisent : *Commande* (acheteur, montant TTC remisé), *Facturation* (payeur, HT/TVA, avoir), *Livraison* (destinataire, colis, adresse de livraison). « Client », « adresse », « statut », « montant » n'y ont pas le même sens : voir [CONTEXT-MAP.md](CONTEXT-MAP.md) et les [glossaires](glossaires/).
- **Les outils de chaque sous-agent** : méthodes annotées `@OutilAgent` dans `<contexte>.outils` (3 + 4 + 3 = 10 outils), décrites dans le langage de leur contexte.
- **Un context map dans le code** : partenariat Commande ↔ Livraison (contrat `commande.api` partagé), client-fournisseur Commande → Facturation avec une **couche anticorruption** (`facturation.acl` : acheteur → payeur, TTC remisé → HT + TVA).
- **Des tests d'architecture ArchUnit** qui imposent les frontières : un outil ne manipule que le modèle de son contexte, seule la couche anticorruption de la Facturation voit la Commande, le domaine ignore les agents, pas de cycle. Un test vérifie que les règles **attrapent** une vraie fuite (`OutilFuyant`, dans les sources de test).
- **Un test de vocabulaire** : aucune description d'outil n'emploie un mot d'un autre contexte, ni le mot « client ».
- **12 scénarios métier** ([scenarios/](scenarios/)) avec réponse attendue, pièges de vocabulaire et vérifications rejouées sur le domaine par les tests, plus une notation déterministe (réussite, erreurs factuelles, fuites de vocabulaire).

## Lancer

```bash
mvn -B verify
```

Java 21 et Maven. Résultat attendu : tous les tests verts (domaine, couche anticorruption, ArchUnit, vocabulaire, scénarios). La CI GitHub Actions lance la même commande.

## État (honnête)

| Élément | État |
|---|---|
| Domaine, 3 contextes, context map, glossaires | fait, testé |
| Outils par contexte (`@OutilAgent`) | fait, testé |
| Couche anticorruption Commande → Facturation | fait, testée |
| Frontières ArchUnit (+ test qui prouve qu'elles attrapent une fuite) | fait |
| 12 scénarios + notation | fait ; réponses de référence vérifiées par les outils |
| Agent unique branché sur un modèle (Spring AI) | **étape suivante** |
| Sous-agents par contexte qui se parlent en A2A | **étape suivante** |
| Interface AG-UI (les deux architectures côte à côte) | **étape suivante** |
| Taux de réussite, tokens, erreurs de vocabulaire : agent unique contre agents découpés | **à mesurer** (aucun chiffre publié) |

Seule mesure disponible aujourd'hui, statique et sans modèle : l'agent unique doit départager **10 outils** dans trois langages ; chaque sous-agent en voit **au plus 4**, dans un seul langage (vérifié par `CatalogueEtVocabulaireTest`). Ce que ça change sur la qualité des réponses reste **à mesurer**.

## Étape suivante : Spring AI, A2A, AG-UI

Plan (versions vérifiées sur Maven Central le 16/11/2026, pas encore dans le `pom.xml`) :

| Brique | Artefact | Version | Rôle |
|---|---|---|---|
| Spring Boot | `org.springframework.boot:spring-boot-starter-parent` | 4.1.2 | socle des agents |
| Spring AI | `org.springframework.ai:spring-ai-bom` | 2.0.2 | `@OutilAgent` → `org.springframework.ai.tool.annotation.Tool`, un `ChatClient` par sous-agent |
| Spring AI A2A | `org.springaicommunity:spring-ai-a2a-server-autoconfigure` | 0.4.0 | chaque sous-agent publié comme serveur A2A (carte d'agent, messages) |
| SDK A2A Java | `org.a2aproject.sdk:a2a-java-sdk-*` | 1.4.0.Final (tiré par Spring AI A2A 0.4.0 ; dernière : 1.5.1.Final) | protocole A2A |
| AG-UI | `com.ag-ui.community:ag-ui-spring-ai-webmvc-boot-starter` | 2.0.0 | interface qui pilote les deux architectures |

Points à vérifier en codant : Spring AI A2A 0.4.0 est compilé contre Spring AI 2.0.1 / Spring Boot 4.1.1 et AG-UI 2.0.0 contre Spring AI 2.0.0 ; l'alignement sur 2.0.2 / 4.1.2 devra être testé.

Ce qui sera construit :

1. **Agent unique** : un `ChatClient` qui reçoit les 10 outils.
2. **Agents découpés** : un routeur + 3 sous-agents (un par contexte), chacun exposé en A2A ; les relations du context map deviennent des règles de routage (la Livraison ne parle jamais à la Facturation).
3. **Banc** : les 12 scénarios × 5 passages sur les deux architectures, mêmes modèle et température ; réponses brutes et tokens versionnés dans `results/`.

## Structure

```
src/main/java/io/github/mekkiamiri/boundedagents/
  agent/          @OutilAgent, catalogue, configurations (agent unique / découpés)
  commande/       domaine/, api/ (langage publié), outils/
  facturation/    domaine/, acl/ (couche anticorruption), outils/
  livraison/      domaine/ (partenariat avec commande.api), outils/
  demo/           jeu de données fictif (racine de composition)
  scenarios/      lecture et notation des scénarios
scenarios/        scenarios.json + protocole
glossaires/       langage omniprésent de chaque contexte
docs/             page de démo et slides (GitHub Pages)
slides/           source des slides
```

## Licence

Apache-2.0, voir [LICENSE](LICENSE). Personnes, entreprises et adresses du jeu de données : toutes fictives.
