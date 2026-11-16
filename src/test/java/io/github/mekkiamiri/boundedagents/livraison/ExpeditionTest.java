package io.github.mekkiamiri.boundedagents.livraison;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.mekkiamiri.boundedagents.livraison.domaine.Destinataire;
import io.github.mekkiamiri.boundedagents.livraison.domaine.Expedition;
import io.github.mekkiamiri.boundedagents.livraison.domaine.StatutLivraison;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExpeditionTest {

    private static Expedition expedition() {
        return new Expedition("C-1", new Destinataire("D"), "1 rue A", "lundi matin");
    }

    @Test
    void l_adresse_de_livraison_change_tant_que_le_colis_n_est_pas_parti() {
        Expedition e = expedition();
        e.modifierAdresse("2 rue B");
        assertEquals("2 rue B", e.adresseLivraison());
        e.expedier(LocalDate.of(2026, 12, 9));
        assertThrows(Expedition.RegleDeLivraison.class, () -> e.modifierAdresse("3 rue C"));
        assertThrows(Expedition.RegleDeLivraison.class, e::arreter);
    }

    @Test
    void un_echec_de_presentation_reprogramme_puis_livre() {
        Expedition e = expedition();
        e.expedier(LocalDate.of(2026, 12, 7));
        e.echecDePresentation(LocalDate.of(2026, 12, 8), "destinataire absent", "vendredi matin");
        assertEquals(StatutLivraison.ECHEC_PRESENTATION, e.statut());
        assertEquals("vendredi matin", e.creneau());
        e.echecDePresentation(LocalDate.of(2026, 12, 11), "adresse introuvable", "lundi matin");
        assertEquals(2, e.echecs());
        assertEquals("adresse introuvable", e.motifEchec());
        e.livrer(LocalDate.of(2026, 12, 14));
        assertEquals(StatutLivraison.LIVREE, e.statut());
    }
}
