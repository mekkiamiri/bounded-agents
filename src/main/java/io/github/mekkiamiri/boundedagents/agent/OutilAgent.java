package io.github.mekkiamiri.boundedagents.agent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque une méthode exposée à un agent comme outil.
 *
 * <p>Équivalent volontairement minimal de {@code org.springframework.ai.tool.annotation.Tool}
 * (Spring AI) : en v0, le domaine reste sans framework ; l'étape suivante remplacera
 * cette annotation par celle de Spring AI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OutilAgent {

    /** Nom de l'outil vu par le modèle. Unique dans tout le catalogue. */
    String nom();

    /** Description lue par le modèle : écrite dans le langage omniprésent du contexte. */
    String description();
}
