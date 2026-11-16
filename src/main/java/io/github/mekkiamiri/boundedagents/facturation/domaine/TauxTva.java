package io.github.mekkiamiri.boundedagents.facturation.domaine;

import java.math.BigDecimal;

/** Taux de TVA applicables (France) : réduit pour les denrées alimentaires, normal pour le reste. */
public enum TauxTva {
    REDUIT(new BigDecimal("5.5")),
    NORMAL(new BigDecimal("20"));

    private final BigDecimal pourcent;

    TauxTva(BigDecimal pourcent) {
        this.pourcent = pourcent;
    }

    public BigDecimal pourcent() {
        return pourcent;
    }
}
