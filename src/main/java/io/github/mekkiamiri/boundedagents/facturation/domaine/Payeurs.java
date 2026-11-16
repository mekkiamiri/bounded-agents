package io.github.mekkiamiri.boundedagents.facturation.domaine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Qui paie pour quel compte. Le compte est le seul identifiant partagé avec la Commande. */
public final class Payeurs {

    private final Map<String, Payeur> parCompte = new HashMap<>();

    public void rattacher(String compte, Payeur payeur) {
        parCompte.put(compte, payeur);
    }

    public Optional<Payeur> pourCompte(String compte) {
        return Optional.ofNullable(parCompte.get(compte));
    }
}
