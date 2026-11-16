package io.github.mekkiamiri.boundedagents.agent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/** Un outil du catalogue : la méthode, l'objet qui la porte et le contexte borné auquel il appartient. */
public record Outil(String contexte, String nom, String description, Method methode, Object cible) {

    public int arite() {
        return methode.getParameterCount();
    }

    public String appeler(List<String> arguments) {
        if (arguments.size() != arite()) {
            throw new IllegalArgumentException(nom + " attend " + arite() + " argument(s), reçu " + arguments.size());
        }
        try {
            return (String) methode.invoke(cible, arguments.toArray());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("échec de l'outil " + nom, e.getCause());
        }
    }
}
