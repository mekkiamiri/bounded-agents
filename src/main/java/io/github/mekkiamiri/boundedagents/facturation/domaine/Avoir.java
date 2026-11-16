package io.github.mekkiamiri.boundedagents.facturation.domaine;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Document qui annule tout ou partie d'une facture (remboursement ou déduction). */
public record Avoir(String numero, BigDecimal montantTTC, LocalDate date, String motif) {
}
