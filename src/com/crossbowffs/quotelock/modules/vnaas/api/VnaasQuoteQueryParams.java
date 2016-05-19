package com.crossbowffs.quotelock.modules.vnaas.api;

import com.crossbowffs.quotelock.modules.vnaas.model.VnaasCharacter;
import com.crossbowffs.quotelock.modules.vnaas.model.VnaasNovel;

public class VnaasQuoteQueryParams extends QueryParams {
    public VnaasQuoteQueryParams setCharacters(long... characterIds) {
        setArrayParam("character_id", characterIds);
        return this;
    }

    public VnaasQuoteQueryParams setCharacters(VnaasCharacter... characters) {
        long[] ids = new long[characters.length];
        for (int i = 0; i < ids.length; ++i) {
            ids[i] = characters[i].getId();
        }
        return setCharacters(ids);
    }

    public VnaasQuoteQueryParams setNovels(long... novelIds) {
        setArrayParam("novel_id", novelIds);
        return this;
    }

    public VnaasQuoteQueryParams setNovels(VnaasNovel... novels) {
        long[] ids = new long[novels.length];
        for (int i = 0; i < ids.length; ++i) {
            ids[i] = novels[i].getId();
        }
        return setNovels(ids);
    }

    public VnaasQuoteQueryParams setContains(String text) {
        setParam("contains", text);
        return this;
    }
}
