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

    /** Amont du context map (service hôte ouvert) : la Commande ne connaît personne. */
    static final ArchRule COMMANDE_NE_CONNAIT_PERSONNE = noClasses()
            .that().resideInAPackage("..commande..")
            .should().dependOnClassesThat().resideInAnyPackage("..facturation..", "..livraison..")
            .because("la Commande est en amont : elle publie, elle ne consomme pas");

    /** Conformiste : la Livraison consomme le langage publié de la Commande (commande.api), et rien d'autre. */
    static final ArchRule LIVRAISON_CONFORMISTE = classes()
            .that().resideInAPackage("..livraison..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "..livraison..", "..commande.api..", "..agent..", "java..")
            .because("la Livraison est conformiste au langage publié de la Commande ; chemins séparés avec la Facturation");

    /** Client-fournisseur avec couche anticorruption : seule la couche acl de la Facturation voit la Commande. */
    static final ArchRule FACTURATION_PASSE_PAR_L_ACL = noClasses()
            .that().resideInAPackage("..facturation..")
            .and().resideOutsideOfPackage("..facturation.acl..")
            .should().dependOnClassesThat().resideInAnyPackage("..commande..", "..livraison..")
            .because("client-fournisseur : la Facturation ne lit la Commande que dans sa couche anticorruption ; chemins séparés avec la Livraison");

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

    /** Spring AI reste dans l'adaptateur ia : le domaine et les outils sont sans framework. */
    static final ArchRule FRAMEWORK_DANS_L_ADAPTATEUR = noClasses()
            .that().resideOutsideOfPackage("..ia..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .because("seul l'adaptateur ia connaît Spring AI");

    /** Les agents ne touchent au domaine qu'à travers les outils du catalogue. */
    static final ArchRule AGENTS_PASSENT_PAR_LES_OUTILS = noClasses()
            .that().resideInAPackage("..ia..")
            .should().dependOnClassesThat().resideInAnyPackage("..domaine..", "..acl..", "..api..")
            .because("un agent ne voit du domaine que ses outils");

    static final ArchRule PAS_DE_CYCLE = slices()
            .matching("io.github.mekkiamiri.boundedagents.(*)..")
            .should().beFreeOfCycles();
}
