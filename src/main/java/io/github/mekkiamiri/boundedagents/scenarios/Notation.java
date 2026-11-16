package io.github.mekkiamiri.boundedagents.scenarios;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Note une réponse d'agent sur un scénario, sans modèle juge : correspondance de fragments
 * après normalisation (minuscules, sans accents, espaces simples).
 *
 * <p>Volontairement simple et déterministe ; une relecture humaine des échecs reste prévue au protocole.
 */
public final class Notation {

    private final List<String> fuitesGlobales;

    public Notation(List<String> fuitesGlobales) {
        this.fuitesGlobales = List.copyOf(fuitesGlobales);
    }

    public Resultat noter(Scenario s, String reponse) {
        String r = normaliser(reponse);
        List<String> manquants = new ArrayList<>();
        for (List<String> groupe : s.doitContenir()) {
            if (groupe.stream().map(Notation::normaliser).noneMatch(r::contains)) {
                manquants.add(String.join(" | ", groupe));
            }
        }
        List<String> erreurs = s.neDoitPasContenir().stream().filter(x -> r.contains(normaliser(x))).toList();
        List<String> fuites = new ArrayList<>();
        for (String f : s.fuites()) {
            fuites.addAll(occurrences(r, f));
        }
        for (String f : fuitesGlobales) {
            fuites.addAll(occurrences(r, f));
        }
        return new Resultat(s.id(), manquants.isEmpty() && erreurs.isEmpty(), manquants, erreurs, fuites);
    }

    private static List<String> occurrences(String reponseNormalisee, String expression) {
        Pattern p = Pattern.compile("\\b" + Pattern.quote(normaliser(expression)) + "\\b");
        Matcher m = p.matcher(reponseNormalisee);
        List<String> trouvees = new ArrayList<>();
        while (m.find()) {
            trouvees.add(expression);
        }
        return trouvees;
    }

    static String normaliser(String texte) {
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toLowerCase(Locale.ROOT)
                .replace('’', '\'')
                .replace('\u00a0', ' ')
                .replace('\u202f', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** Résultat d'une réponse : réussite, fragments manquants, erreurs factuelles et fuites de vocabulaire. */
    public record Resultat(String scenario, boolean reussi, List<String> manquants, List<String> erreurs, List<String> fuites) {
    }
}
