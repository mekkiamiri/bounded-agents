package io.github.mekkiamiri.boundedagents.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mekkiamiri.boundedagents.agent.CatalogueOutils;
import io.github.mekkiamiri.boundedagents.demo.JeuDeDonnees;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

/**
 * Câblage Spring AI des deux architectures, avec un faux modèle scénarisé : aucun appel réseau, aucun modèle en CI.
 * Le faux modèle demande des outils comme le ferait un vrai ; Spring AI les exécute sur le vrai domaine.
 */
class ArchitecturesTest {

    /** Faux modèle : rejoue un script et note les outils qu'on lui a proposés à chaque appel. */
    static final class FauxModele implements ChatModel {
        final Function<Prompt, AssistantMessage> script;
        final List<Set<String>> outilsVus = new ArrayList<>();

        FauxModele(Function<Prompt, AssistantMessage> script) {
            this.script = script;
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            Set<String> outils = new TreeSet<>();
            if (prompt.getOptions() instanceof ToolCallingChatOptions o) {
                o.getToolCallbacks().forEach(t -> outils.add(t.getToolDefinition().name()));
            }
            outilsVus.add(outils);
            return new ChatResponse(List.of(new Generation(script.apply(prompt))),
                    ChatResponseMetadata.builder().usage(new DefaultUsage(100, 20)).build());
        }

        @Override
        public ChatOptions getOptions() {
            return ToolCallingChatOptions.builder().build();
        }
    }

    private static AssistantMessage appels(String... nomEtArguments) {
        List<AssistantMessage.ToolCall> appels = new ArrayList<>();
        for (int i = 0; i < nomEtArguments.length; i += 2) {
            appels.add(new AssistantMessage.ToolCall("appel-" + i, "function", nomEtArguments[i], nomEtArguments[i + 1]));
        }
        return AssistantMessage.builder().content("").toolCalls(appels).build();
    }

    /** Si les outils ont répondu, le faux modèle recopie leurs réponses ; sinon il appelle les outils demandés. */
    private static AssistantMessage repondreOuAppeler(Prompt p, AssistantMessage appels) {
        List<Message> messages = p.getInstructions();
        if (messages.get(messages.size() - 1) instanceof ToolResponseMessage t) {
            return new AssistantMessage(t.getResponses().stream()
                    .map(ToolResponseMessage.ToolResponse::responseData).collect(Collectors.joining(" | ")));
        }
        return appels;
    }

    private final CatalogueOutils catalogue = JeuDeDonnees.brulerie().catalogue();
    private final Map<String, String> glossaires;

    ArchitecturesTest() throws Exception {
        glossaires = Architectures.glossaires(Path.of("."));
    }

    @Test
    void l_agent_unique_voit_les_dix_outils_et_les_appelle_sur_le_domaine() {
        String ref = "{\"referenceCommande\":\"C-1042\"}";
        FauxModele modele = new FauxModele(p -> repondreOuAppeler(p, appels(
                "statutCommande", "{\"numero\":\"C-1042\"}", "suivreExpedition", ref, "consulterFacture", ref)));
        ModeleCompte compte = new ModeleCompte(modele);

        String reponse = Architectures.agentUnique(compte, catalogue, glossaires).repondre("La commande C-1042 est-elle terminée ?");

        assertEquals(10, modele.outilsVus.get(0).size(), modele.outilsVus.toString());
        assertTrue(reponse.contains("colis livré le 2026-11-06"), reponse);
        assertTrue(reponse.contains("facture en retard"), reponse);
        assertEquals(2, compte.appels());
        assertEquals(200, compte.tokensEntree());
    }

    @Test
    void le_routeur_ne_voit_que_trois_sous_agents_qui_ne_voient_que_leurs_outils() {
        FauxModele modele = new FauxModele(p -> {
            String systeme = p.getSystemMessage().getText();
            if (systeme.contains("Tu es le routeur")) {
                return repondreOuAppeler(p, appels(
                        "demanderACommande", "{\"question\":\"Qui est l'acheteur de C-1042 ?\"}",
                        "demanderAFacturation", "{\"question\":\"Qui est le payeur de C-1042 ?\"}",
                        "demanderALivraison", "{\"question\":\"Qui est le destinataire de C-1042 ?\"}"));
            }
            String ref = "{\"referenceCommande\":\"C-1042\"}";
            if (systeme.contains("sous-agent Commande")) {
                return repondreOuAppeler(p, appels("consulterCommande", "{\"numero\":\"C-1042\"}"));
            }
            if (systeme.contains("sous-agent Facturation")) {
                return repondreOuAppeler(p, appels("consulterFacture", ref));
            }
            return repondreOuAppeler(p, appels("suivreExpedition", ref));
        });

        String reponse = Architectures.agentsDecoupes(modele, catalogue, glossaires).repondre("Qui est le client de la commande C-1042 ?");

        assertEquals(Set.of("demanderACommande", "demanderAFacturation", "demanderALivraison"), modele.outilsVus.get(0));
        assertTrue(modele.outilsVus.contains(Set.of("consulterCommande", "demanderAnnulation", "statutCommande")), modele.outilsVus.toString());
        assertTrue(modele.outilsVus.contains(Set.of("arreterExpedition", "modifierAdresseLivraison", "suivreExpedition")), modele.outilsVus.toString());
        assertTrue(modele.outilsVus.stream().allMatch(s -> s.size() <= 4), modele.outilsVus.toString());
        assertTrue(reponse.contains("Inès Morel") && reponse.contains("Studio Pivot SAS") && reponse.contains("Karim Benali"), reponse);
    }

    @Test
    void un_argument_manquant_est_signale_au_modele_sans_exception() {
        OutilSpringAi outil = new OutilSpringAi(catalogue.outil("suivreExpedition").orElseThrow());
        assertEquals("erreur : argument manquant « referenceCommande »", outil.call("{}"));
        assertTrue(outil.getToolDefinition().inputSchema().contains("\"referenceCommande\""));
    }
}
