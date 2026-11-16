package io.github.mekkiamiri.boundedagents.commande.domaine;

import io.github.mekkiamiri.boundedagents.commande.api.CommandeValidee;
import io.github.mekkiamiri.boundedagents.commande.api.CommandesPubliees;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dépôt en mémoire des commandes. Publie vers les autres contextes uniquement les commandes validées,
 * sous la forme du contrat {@link CommandeValidee} (langage publié).
 */
public final class Commandes implements CommandesPubliees {

    private final Map<String, Commande> parNumero = new LinkedHashMap<>();

    public void enregistrer(Commande commande) {
        parNumero.put(commande.numero(), commande);
    }

    public Optional<Commande> trouver(String numero) {
        return Optional.ofNullable(parNumero.get(numero));
    }

    @Override
    public Optional<CommandeValidee> commandeValidee(String numero) {
        return trouver(numero).filter(c -> c.statut() == StatutCommande.VALIDEE).map(Commandes::publier);
    }

    static CommandeValidee publier(Commande c) {
        List<CommandeValidee.Ligne> lignes = c.lignes().stream()
                .map(l -> new CommandeValidee.Ligne(l.produit(), l.categorie().name(), l.quantite(), c.totalTTCRemise(l)))
                .toList();
        ConsignesDeLivraison k = c.consignes();
        return new CommandeValidee(c.numero(), c.acheteur().compte(), c.acheteur().nom(), lignes, c.totalTTC(),
                c.datePassage(), k.nomDestinataire(), k.adresse(), k.creneauSouhaite());
    }
}
