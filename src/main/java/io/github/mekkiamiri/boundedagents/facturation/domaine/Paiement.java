package io.github.mekkiamiri.boundedagents.facturation.domaine;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Paiement(BigDecimal montant, LocalDate date) {
}
