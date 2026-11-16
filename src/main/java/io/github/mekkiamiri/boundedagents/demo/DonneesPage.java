package io.github.mekkiamiri.boundedagents.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.mekkiamiri.boundedagents.agent.Outil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Génère {@code docs/donnees.js} pour la page de démo, à partir des sources de vérité :
 * le catalogue d'outils (code), les glossaires ({@code glossaires/*.md}) et {@code scenarios/scenarios.json}.
 *
 * <p>Régénérer : {@code mvn -B test -Dtest=PageCoherenceTest -DregenererPage=true}.
 * Sans cette option, le test vérifie que le fichier est à jour (mode « --check »).
 */
public final class DonneesPage {

    public static final Path FICHIER = Path.of("docs", "donnees.js");
    private static final List<String> CONTEXTES = List.of("commande", "facturation", "livraison");

    private DonneesPage() {
    }

    public static String generer(Path racine) throws IOException {
        ObjectMapper json = new ObjectMapper();
        ObjectNode donnees = json.createObjectNode();

        ArrayNode outils = donnees.putArray("outils");
        for (Outil o : JeuDeDonnees.brulerie().catalogue().tous()) {
            outils.addObject().put("contexte", o.contexte()).put("nom", o.nom()).put("description", o.description());
        }

        ObjectNode glossaires = donnees.putObject("glossaires");
        for (String contexte : CONTEXTES) {
            ArrayNode termes = glossaires.putArray(contexte);
            for (String ligne : Files.readAllLines(racine.resolve("glossaires").resolve(contexte + ".md"), StandardCharsets.UTF_8)) {
                String[] cellules = ligne.split("\\|");
                if (cellules.length < 3 || ligne.startsWith("|---") || cellules[1].trim().equals("Terme")) {
                    continue;
                }
                termes.addArray().add(cellules[1].trim()).add(cellules[2].trim().replace("**", "").replace("`", ""));
            }
        }

        JsonNode jeu = json.readTree(racine.resolve("scenarios").resolve("scenarios.json").toFile());
        donnees.set("jeu", jeu);

        return "// Généré par DonneesPage.java depuis le catalogue d'outils, glossaires/ et scenarios/ : ne pas modifier à la main.\n"
                + "window.DONNEES = " + json.writerWithDefaultPrettyPrinter().writeValueAsString(donnees) + ";\n";
    }

    public static void main(String[] args) throws IOException {
        Files.writeString(FICHIER, generer(Path.of(".")), StandardCharsets.UTF_8);
        System.out.println("écrit " + FICHIER);
    }
}
