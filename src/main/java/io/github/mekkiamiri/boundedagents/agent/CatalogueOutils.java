package io.github.mekkiamiri.boundedagents.agent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Inventaire des outils exposés aux agents, rangés par contexte borné.
 *
 * <p>Le contexte d'un outil est déduit de son paquet :
 * {@code ...boundedagents.<contexte>.outils}.
 */
public final class CatalogueOutils {

    private final List<Outil> outils;

    private CatalogueOutils(List<Outil> outils) {
        this.outils = List.copyOf(outils);
    }

    public static CatalogueOutils depuis(Object... porteurs) {
        List<Outil> trouves = new ArrayList<>();
        for (Object porteur : porteurs) {
            String contexte = contexteDe(porteur.getClass());
            for (Method m : porteur.getClass().getMethods()) {
                OutilAgent a = m.getAnnotation(OutilAgent.class);
                if (a == null) {
                    continue;
                }
                if (m.getReturnType() != String.class || !Modifier.isPublic(m.getModifiers())) {
                    throw new IllegalStateException(m + " : un outil est public et renvoie un texte");
                }
                for (Class<?> p : m.getParameterTypes()) {
                    if (p != String.class) {
                        throw new IllegalStateException(m + " : les paramètres d'un outil sont des textes");
                    }
                }
                trouves.add(new Outil(contexte, a.nom(), a.description(), m, porteur));
            }
        }
        trouves.sort(Comparator.comparing(Outil::contexte).thenComparing(Outil::nom));
        long distincts = trouves.stream().map(Outil::nom).distinct().count();
        if (distincts != trouves.size()) {
            throw new IllegalStateException("deux outils portent le même nom");
        }
        return new CatalogueOutils(trouves);
    }

    static String contexteDe(Class<?> type) {
        String[] parties = type.getPackageName().split("\\.");
        if (parties.length < 2 || !parties[parties.length - 1].equals("outils")) {
            throw new IllegalStateException(type + " : les outils vivent dans le paquet <contexte>.outils");
        }
        return parties[parties.length - 2];
    }

    public List<Outil> tous() {
        return outils;
    }

    public Optional<Outil> outil(String nom) {
        return outils.stream().filter(o -> o.nom().equals(nom)).findFirst();
    }

    public String appeler(String nom, List<String> arguments) {
        return outil(nom).orElseThrow(() -> new IllegalArgumentException("outil inconnu : " + nom)).appeler(arguments);
    }

    public Map<String, List<Outil>> parContexte() {
        return outils.stream().collect(Collectors.groupingBy(Outil::contexte, LinkedHashMap::new, Collectors.toList()));
    }
}
