package com.crossbowffs.quotelock.modules.vnaas.model;

public class VnaasNovel {
    private final long mId;
    private final String mName;
    private final VnaasCharacter[] mCharacters;

    public VnaasNovel(long id, String name, VnaasCharacter[] characters) {
        mId = id;
        mName = name;
        mCharacters = characters;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public VnaasCharacter[] getCharacters() {
        return mCharacters;
    }
}
