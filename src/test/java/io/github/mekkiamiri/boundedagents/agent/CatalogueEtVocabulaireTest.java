package io.github.mekkiamiri.boundedagents.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mekkiamiri.boundedagents.demo.JeuDeDonnees;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CatalogueEtVocabulaireTest {

    private final CatalogueOutils catalogue = JeuDeDonnees.brulerie().catalogue();

    /** Mots qui appartiennent à un autre contexte (ou à aucun, comme « client ») : interdits dans les descriptions. */
    private static final Map<String, List<String>> MOTS_ETRANGERS = Map.of(
            "commande", List.of("client", "payeur", "facture", "colis", "destinataire", "avoir"),
            "facturation", List.of("client", "acheteur", "panier", "colis", "destinataire"),
            "livraison", List.of("client", "acheteur", "payeur", "panier", "facture", "avoir", "ttc"));

    @Test
    void chaque_contexte_expose_ses_outils() {
        Map<String, List<Outil>> parContexte = catalogue.parContexte();
        assertEquals(List.of("commande", "facturation", "livraison"), List.copyOf(parContexte.keySet()));
        assertEquals(List.of("consulterCommande", "demanderAnnulation", "statutCommande"), noms(parContexte.get("commande")));
        assertEquals(List.of("consulterFacture", "emettreFacture", "listerAvoirs", "resteARegler"), noms(parContexte.get("facturation")));
        assertEquals(List.of("arreterExpedition", "modifierAdresseLivraison", "suivreExpedition"), noms(parContexte.get("livraison")));
    }

    @Test
    void les_descriptions_parlent_le_langage_de_leur_contexte() {
        for (Outil o : catalogue.tous()) {
            String description = normaliser(o.description());
            for (String mot : MOTS_ETRANGERS.get(o.contexte())) {
                boolean present = Pattern.compile("\\b" + mot + "s?\\b").matcher(description).find();
                assertTrue(!present, o.nom() + " (" + o.contexte() + ") emploie « " + mot + " » : " + o.description());
            }
        }
    }

    @Test
    void l_agent_unique_doit_departager_tous_les_outils() {
        Configuration unique = Configuration.agentUnique(catalogue);
        Configuration decoupes = Configuration.agentsDecoupes(catalogue);
        assertEquals(10, unique.outilsParAgentMax());
        assertEquals(4, decoupes.outilsParAgentMax());
        assertTrue(decoupes.caracteresDescriptionsMax() < unique.caracteresDescriptionsMax());
        assertEquals(3, decoupes.agents().size());
    }

    @Test
    void un_outil_s_appelle_par_son_nom() {
        assertEquals("C-1042 : commande validée", catalogue.appeler("statutCommande", List.of("C-1042")));
    }

    private static List<String> noms(List<Outil> outils) {
        return outils.stream().map(Outil::nom).toList();
    }

    private static String normaliser(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
