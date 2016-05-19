package com.crossbowffs.quotelock.modules.vnaas.model;

public class VnaasQuote {
    private final String mText;
    private final VnaasCharacter mCharacter;
    private final VnaasNovel mNovel;

    public VnaasQuote(String text, VnaasCharacter character, VnaasNovel novel) {
        mText = text;
        mCharacter = character;
        mNovel = novel;
    }

    public String getText() {
        return mText;
    }

    public VnaasCharacter getCharacter() {
        return mCharacter;
    }

    public VnaasNovel getNovel() {
        return mNovel;
    }
}
