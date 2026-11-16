package io.github.mekkiamiri.boundedagents.ia;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

/** Enveloppe un modèle et additionne les tokens de chaque appel, routeur et sous-agents compris. */
public final class ModeleCompte implements ChatModel {

    private final ChatModel delegue;
    private final AtomicInteger appels = new AtomicInteger();
    private final AtomicLong tokensEntree = new AtomicLong();
    private final AtomicLong tokensSortie = new AtomicLong();

    public ModeleCompte(ChatModel delegue) {
        this.delegue = delegue;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        ChatResponse r = delegue.call(prompt);
        appels.incrementAndGet();
        Usage u = r.getMetadata() == null ? null : r.getMetadata().getUsage();
        if (u != null) {
            tokensEntree.addAndGet(u.getPromptTokens() == null ? 0 : u.getPromptTokens());
            tokensSortie.addAndGet(u.getCompletionTokens() == null ? 0 : u.getCompletionTokens());
        }
        return r;
    }

    /** ChatClient part de ces options pour y ajouter les outils : il faut celles du vrai modèle. */
    @Override
    public ChatOptions getOptions() {
        return delegue.getOptions();
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return delegue.getDefaultOptions();
    }

    public int appels() { return appels.get(); }
    public long tokensEntree() { return tokensEntree.get(); }
    public long tokensSortie() { return tokensSortie.get(); }
}
