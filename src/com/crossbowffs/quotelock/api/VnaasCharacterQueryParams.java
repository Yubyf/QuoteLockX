package com.crossbowffs.quotelock.api;

import com.crossbowffs.quotelock.model.VnaasNovel;

public class VnaasCharacterQueryParams extends QueryParams {
    private static final String KEY_NOVEL_ID = "novel_id";
    private static final String KEY_NOVEL_NAME = "name";

    public VnaasCharacterQueryParams setNovel(long novelId) {
        setParam(KEY_NOVEL_ID, novelId);
        return this;
    }

    public VnaasCharacterQueryParams setNovel(VnaasNovel novel) {
        return setNovel(novel.getId());
    }

    public VnaasCharacterQueryParams setName(String name) {
        setParam(KEY_NOVEL_NAME, name);
        return this;
    }
}
