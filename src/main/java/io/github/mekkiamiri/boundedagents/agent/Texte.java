package io.github.mekkiamiri.boundedagents.agent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** Mise en forme des réponses d'outils (texte lu par le modèle). */
public final class Texte {

    private Texte() {
    }

    public static String euros(BigDecimal montant) {
        return String.format(Locale.FRANCE, "%.2f €", montant.setScale(2, RoundingMode.HALF_UP));
    }
}
