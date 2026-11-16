package io.github.mekkiamiri.boundedagents.commande.domaine;

import io.github.mekkiamiri.boundedagents.commande.api.CommandeValidee;
import io.github.mekkiamiri.boundedagents.commande.api.CommandesPubliees;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Dépôt en mémoire des commandes et service hôte ouvert de la Commande :
 * <ul>
 *   <li>il publie l'événement du domaine {@link CommandeValidee} aux abonnés, au moment de la validation ;</li>
 *   <li>il répond aux questions sur une commande engagée, dans ce même langage publié.</li>
 * </ul>
 */
public final class Commandes implements CommandesPubliees {

    private final Map<String, Commande> parNumero = new LinkedHashMap<>();
    private final List<Consumer<CommandeValidee>> abonnes = new ArrayList<>();

    public void enregistrer(Commande commande) {
        parNumero.put(commande.numero(), commande);
    }

    public Optional<Commande> trouver(String numero) {
        return Optional.ofNullable(parNumero.get(numero));
    }

    @Override
    public void abonner(Consumer<CommandeValidee> abonne) {
        abonnes.add(abonne);
    }

    /** Valide la commande puis publie l'événement {@link CommandeValidee}. */
    public CommandeValidee valider(String numero, LocalDate date) {
        Commande c = trouver(numero).orElseThrow(() -> new IllegalArgumentException("commande inconnue : " + numero));
        c.valider(date);
        CommandeValidee evenement = publier(c);
        abonnes.forEach(a -> a.accept(evenement));
        return evenement;
    }

    /** Une commande avec une annulation en attente reste publiée : rien n'est encore confirmé. */
    @Override
    public Optional<CommandeValidee> commandeValidee(String numero) {
        return trouver(numero).filter(Commande::engagee).map(Commandes::publier);
    }

    static CommandeValidee publier(Commande c) {
        List<CommandeValidee.Ligne> lignes = c.lignes().stream()
                .map(l -> new CommandeValidee.Ligne(l.produit(), l.categorie().name(), l.quantite(), c.totalTTCRemise(l)))
                .toList();
        ConsignesDeLivraison k = c.consignes();
        return new CommandeValidee(c.numero(), c.acheteur().compte(), c.acheteur().nom(), lignes, c.totalTTC(),
                c.datePassage(), c.dateValidation(), k.nomDestinataire(), k.adresse(), k.creneauSouhaite());
    }
}
