package io.github.mekkiamiri.boundedagents.scenarios;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Note une réponse d'agent sur un scénario, sans modèle juge : correspondance de fragments
 * après normalisation (minuscules, sans accents, espaces simples).
 *
 * <ul>
 *   <li>Un fragment se compare mot entier : « 64,80 » ne valide pas « 164,80 ».</li>
 *   <li>Un fragment nié dans sa proposition (« non », « pas », « jamais »… dans les 3 mots qui précèdent,
 *       sans franchir une ponctuation) ne compte pas : « non validée » ne valide pas « validée ».
 *       Un fragment qui commence lui-même par une négation (« pas validée ») se compare tel quel.</li>
 *   <li>Les fuites de vocabulaire se comptent mot entier, négation ou pas.</li>
 * </ul>
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
            if (groupe.stream().noneMatch(f -> affirme(r, f))) {
                manquants.add(String.join(" | ", groupe));
            }
        }
        List<String> erreurs = s.neDoitPasContenir().stream().filter(x -> affirme(r, x)).toList();
        List<String> fuites = new ArrayList<>();
        for (String f : s.fuites()) {
            fuites.addAll(occurrences(r, f));
        }
        for (String f : fuitesGlobales) {
            fuites.addAll(occurrences(r, f));
        }
        return new Resultat(s.id(), manquants.isEmpty() && erreurs.isEmpty(), manquants, erreurs, fuites);
    }

    private static final Set<String> NEGATIONS = Set.of("non", "pas", "jamais", "ni", "aucun", "aucune", "sans", "plus");

    /** Vrai si le fragment apparaît en mot entier, au moins une fois sans négation devant lui. */
    static boolean affirme(String reponseNormalisee, String fragment) {
        String f = normaliser(fragment);
        boolean fragmentNegatif = NEGATIONS.contains(f.split("[ ']")[0]);
        Matcher m = motEntier(f).matcher(reponseNormalisee);
        while (m.find()) {
            if (fragmentNegatif || !nie(reponseNormalisee.substring(0, m.start()))) {
                return true;
            }
        }
        return false;
    }

    private static boolean nie(String avant) {
        String proposition = avant.replaceAll("^.*[.;:!?,()]", "");
        String[] mots = proposition.trim().split("[ ']+");
        for (int i = Math.max(0, mots.length - 3); i < mots.length; i++) {
            if (NEGATIONS.contains(mots[i])) {
                return true;
            }
        }
        return false;
    }

    private static Pattern motEntier(String fragmentNormalise) {
        return Pattern.compile("(?<![a-z0-9])" + Pattern.quote(fragmentNormalise) + "(?![a-z0-9])");
    }

    private static List<String> occurrences(String reponseNormalisee, String expression) {
        Pattern p = motEntier(normaliser(expression));
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
