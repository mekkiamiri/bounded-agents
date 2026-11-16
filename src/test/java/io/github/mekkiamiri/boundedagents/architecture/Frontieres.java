package io.github.mekkiamiri.boundedagents.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import io.github.mekkiamiri.boundedagents.agent.OutilAgent;
import com.tngtech.archunit.lang.ArchRule;

/** Les frontières du context map, écrites comme des règles exécutables. */
final class Frontieres {

    private Frontieres() {
    }

    /** Amont du context map : la Commande ne connaît personne. */
    static final ArchRule COMMANDE_NE_CONNAIT_PERSONNE = noClasses()
            .that().resideInAPackage("..commande..")
            .should().dependOnClassesThat().resideInAnyPackage("..facturation..", "..livraison..")
            .because("la Commande est en amont : elle publie, elle ne consomme pas");

    /** Partenariat : la Livraison lit le contrat publié de la Commande, rien d'autre. */
    static final ArchRule LIVRAISON_LIT_SEULEMENT_LE_CONTRAT = noClasses()
            .that().resideInAPackage("..livraison..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..commande.domaine..", "..commande.outils..", "..facturation..")
            .because("partenariat Commande ↔ Livraison : seul le contrat commande.api est partagé");

    /** Client-fournisseur avec couche anticorruption : seule la couche acl de la Facturation voit la Commande. */
    static final ArchRule FACTURATION_PASSE_PAR_L_ACL = noClasses()
            .that().resideInAPackage("..facturation..")
            .and().resideOutsideOfPackage("..facturation.acl..")
            .should().dependOnClassesThat().resideInAnyPackage("..commande..", "..livraison..")
            .because("la Facturation ne parle le langage de la Commande que dans sa couche anticorruption");

    static final ArchRule ACL_NE_LIT_QUE_LE_CONTRAT = classes()
            .that().resideInAPackage("..facturation.acl..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("..facturation..", "..commande.api..", "java..")
            .because("la couche anticorruption traduit le contrat publié, pas le modèle interne de la Commande");

    static ArchRule outilsDuContexte(String contexte) {
        return classes()
                .that().resideInAPackage(".." + contexte + ".outils..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(".." + contexte + "..", "..agent..", "java..")
                .because("un outil de " + contexte + " ne manipule que le modèle de " + contexte);
    }

    static final ArchRule DOMAINE_IGNORE_LES_AGENTS = noClasses()
            .that().resideInAnyPackage("..domaine..", "..api..", "..acl..")
            .should().dependOnClassesThat().resideInAPackage("..agent..")
            .because("le domaine ne sait pas qu'il est piloté par des agents");

    static final ArchRule OUTILS_DANS_LEUR_PAQUET = methods()
            .that().areAnnotatedWith(OutilAgent.class)
            .should().beDeclaredInClassesThat().resideInAPackage("..outils..")
            .because("les outils d'un sous-agent sont rangés dans <contexte>.outils");

    static final ArchRule PAS_DE_CYCLE = slices()
            .matching("io.github.mekkiamiri.boundedagents.(*)..")
            .should().beFreeOfCycles();
}
