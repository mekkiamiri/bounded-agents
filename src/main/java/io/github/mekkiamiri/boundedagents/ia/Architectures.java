package io.github.mekkiamiri.boundedagents.ia;

import io.github.mekkiamiri.boundedagents.agent.CatalogueOutils;
import io.github.mekkiamiri.boundedagents.agent.Outil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/**
 * Les deux architectures comparées par le talk, branchées sur Spring AI.
 *
 * <ul>
 *   <li>Agent unique : un {@link ChatClient}, les 10 outils, les trois glossaires dans le même prompt.</li>
 *   <li>Agents découpés : un routeur qui ne voit que trois « sous-agents » ; chaque sous-agent a son
 *       glossaire et ses outils, rien d'autre. Le routeur suit le context map.</li>
 * </ul>
 * Même modèle et mêmes outils dans les deux cas : seul le découpage change.
 */
public final class Architectures {

    public static final List<String> CONTEXTES = List.of("commande", "facturation", "livraison");

    private Architectures() {
    }

    public static Map<String, String> glossaires(Path racine) throws IOException {
        Map<String, String> g = new LinkedHashMap<>();
        for (String c : CONTEXTES) {
            g.put(c, Files.readString(racine.resolve("glossaires").resolve(c + ".md"), StandardCharsets.UTF_8));
        }
        return g;
    }

    public static Agent agentUnique(ChatModel modele, CatalogueOutils catalogue, Map<String, String> glossaires) {
        String systeme = """
                Tu es l'assistant d'une brûlerie de café qui vend en ligne. Tu réponds en français, brièvement,
                en t'appuyant uniquement sur les résultats de tes outils. Voici les glossaires des trois équipes :

                """ + String.join("\n\n", glossaires.values());
        return client(modele, systeme, adapter(catalogue.tous()));
    }

    public static Agent agentsDecoupes(ChatModel modele, CatalogueOutils catalogue, Map<String, String> glossaires) {
        List<ToolCallback> sousAgents = new ArrayList<>();
        for (Map.Entry<String, List<Outil>> e : catalogue.parContexte().entrySet()) {
            String contexte = e.getKey();
            String systeme = """
                    Tu es le sous-agent %s d'une brûlerie de café. Tu ne connais que ce contexte borné.
                    Tu réponds en français, brièvement, avec le vocabulaire de ton glossaire et les résultats de tes outils.
                    Si la question ne relève pas de ton contexte, dis-le sans deviner.

                    %s""".formatted(nomContexte(contexte), glossaires.get(contexte));
            Agent sousAgent = client(modele, systeme, adapter(e.getValue()));
            sousAgents.add(new SousAgentSpringAi("demanderA" + nomContexte(contexte),
                    descriptionSousAgent(contexte), sousAgent));
        }
        String routeur = """
                Tu es le routeur d'une brûlerie de café. Tu n'as pas d'outil métier : tu interroges trois sous-agents.
                Context map : la Commande publie les commandes validées ; la Facturation les traduit (acheteur → payeur,
                TTC → HT + TVA) ; la Livraison les suit telles quelles ; Livraison et Facturation ne se parlent pas.
                Les mots courants sont ambigus : « client » = acheteur (Commande), payeur (Facturation) ou destinataire
                (Livraison) ; « adresse » = adresse de facturation ou de livraison ; « statut » existe dans chaque contexte.
                Traduis la question dans le vocabulaire de chaque sous-agent concerné, interroge tous ceux qui sont
                concernés, puis réponds en français en nommant le contexte de chaque information, sans le mot « client ».""";
        return client(modele, routeur, sousAgents);
    }

    static List<ToolCallback> adapter(List<Outil> outils) {
        return outils.stream().<ToolCallback>map(OutilSpringAi::new).toList();
    }

    private static Agent client(ChatModel modele, String systeme, List<ToolCallback> outils) {
        ChatClient client = ChatClient.builder(modele).defaultSystem(systeme).defaultToolCallbacks(outils).build();
        return question -> client.prompt().user(question).call().content();
    }

    static String nomContexte(String contexte) {
        return Character.toUpperCase(contexte.charAt(0)) + contexte.substring(1);
    }

    private static String descriptionSousAgent(String contexte) {
        return switch (contexte) {
            case "commande" -> "Sous-agent Commande : acheteur, panier, remise, montant TTC, statut de la commande, demande d'annulation.";
            case "facturation" -> "Sous-agent Facturation : payeur, adresse de facturation, HT, TVA, TTC, échéance, reste à régler, avoirs.";
            case "livraison" -> "Sous-agent Livraison : destinataire, adresse de livraison, colis, créneau, statut d'acheminement.";
            default -> throw new IllegalArgumentException(contexte);
        };
    }
}
