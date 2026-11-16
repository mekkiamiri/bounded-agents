package io.github.mekkiamiri.boundedagents.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FrontieresTest {

    private static JavaClasses code;

    @BeforeAll
    static void importer() {
        code = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.mekkiamiri.boundedagents");
    }

    @Test
    void la_commande_ne_connait_personne() {
        Frontieres.COMMANDE_NE_CONNAIT_PERSONNE.check(code);
    }

    @Test
    void la_livraison_est_conformiste_au_langage_publie_de_la_commande() {
        Frontieres.LIVRAISON_CONFORMISTE.check(code);
    }

    @Test
    void la_facturation_passe_par_sa_couche_anticorruption() {
        Frontieres.FACTURATION_PASSE_PAR_L_ACL.check(code);
        Frontieres.ACL_NE_LIT_QUE_LE_CONTRAT.check(code);
    }

    @ParameterizedTest
    @ValueSource(strings = {"commande", "facturation", "livraison"})
    void un_outil_ne_manipule_que_son_contexte(String contexte) {
        Frontieres.outilsDuContexte(contexte).check(code);
    }

    @Test
    void le_domaine_ignore_les_agents() {
        Frontieres.DOMAINE_IGNORE_LES_AGENTS.check(code);
    }

    @Test
    void les_outils_sont_dans_leur_paquet() {
        Frontieres.OUTILS_DANS_LEUR_PAQUET.check(code);
    }

    @Test
    void spring_ai_reste_dans_l_adaptateur_et_les_agents_passent_par_les_outils() {
        Frontieres.FRAMEWORK_DANS_L_ADAPTATEUR.check(code);
        Frontieres.AGENTS_PASSENT_PAR_LES_OUTILS.check(code);
    }

    @Test
    void pas_de_cycle_entre_paquets() {
        Frontieres.PAS_DE_CYCLE.check(code);
    }
}
