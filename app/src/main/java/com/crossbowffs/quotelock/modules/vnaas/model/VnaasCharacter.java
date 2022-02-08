package com.crossbowffs.quotelock.modules.vnaas.model;

public class VnaasCharacter {
    private final long mId;
    private final String mName;
    private final VnaasNovel[] mNovels;

    public VnaasCharacter(long id, String name, VnaasNovel[] novels) {
        mId = id;
        mName = name;
        mNovels = novels;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public VnaasNovel[] getNovels() {
        return mNovels;
    }
}
