package io.github.mekkiamiri.boundedagents.commande.api;

import java.util.Optional;
import java.util.function.Consumer;

/** Service hôte ouvert de la Commande : abonnement aux événements et consultation, dans le langage publié. */
public interface CommandesPubliees {

    /** Reçoit chaque {@link CommandeValidee} au moment de la validation. */
    void abonner(Consumer<CommandeValidee> abonne);

    /** Vide si la commande n'existe pas, n'est pas validée ou est annulée. */
    Optional<CommandeValidee> commandeValidee(String numero);
}
