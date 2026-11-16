package io.github.mekkiamiri.boundedagents.commande.api;

import java.util.Optional;

/** Port publié par la Commande. Vide si la commande n'existe pas ou n'est pas validée. */
public interface CommandesPubliees {

    Optional<CommandeValidee> commandeValidee(String numero);
}
