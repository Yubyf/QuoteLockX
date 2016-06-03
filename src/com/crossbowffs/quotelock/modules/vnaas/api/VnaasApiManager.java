package com.crossbowffs.quotelock.modules.vnaas.api;

import com.crossbowffs.quotelock.modules.vnaas.model.VnaasCharacter;
import com.crossbowffs.quotelock.modules.vnaas.model.VnaasNovel;
import com.crossbowffs.quotelock.modules.vnaas.model.VnaasQuote;
import com.crossbowffs.quotelock.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class VnaasApiManager {
    private static final String NOVELS_URL = "/novels";
    private static final String CHARACTERS_URL = "/characters";
    private static final String RANDOM_QUOTE_URL = "/random_quote";

    private final String mBaseUrl;

    public VnaasApiManager(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public VnaasNovel[] getNovels(VnaasNovelQueryParams params) throws IOException {
        String url = params.buildUrl(mBaseUrl + NOVELS_URL);
        JSONArray json = fetchJsonArray(url);
        try {
            return jsonArrayToNovels(json);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public VnaasNovel getNovel(long novelId) throws IOException {
        String url = mBaseUrl + NOVELS_URL + "/" + novelId;
        JSONObject json = fetchJsonObject(url);
        try {
            return jsonToNovel(json);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public VnaasCharacter[] getCharacters(VnaasCharacterQueryParams params) throws IOException {
        String url = params.buildUrl(mBaseUrl + CHARACTERS_URL);
        JSONArray json = fetchJsonArray(url);
        try {
            return jsonArrayToCharacters(json);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public VnaasCharacter getCharacter(long characterId) throws IOException {
        String url = mBaseUrl + CHARACTERS_URL + "/" + characterId;
        JSONObject json = fetchJsonObject(url);
        try {
            return jsonToCharacter(json);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public VnaasQuote getRandomQuote(VnaasQuoteQueryParams params) throws IOException {
        String url = params.buildUrl(mBaseUrl + RANDOM_QUOTE_URL);
        JSONObject json = fetchJsonObject(url);
        try {
            return jsonToQuote(json);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private JSONObject fetchJsonObject(String urlString) throws IOException {
        String jsonString = IOUtils.downloadString(urlString);
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private JSONArray fetchJsonArray(String urlString) throws IOException {
        String jsonString = IOUtils.downloadString(urlString);
        try {
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private VnaasNovel jsonToNovel(JSONObject jsonObject) throws JSONException {
        long id = jsonObject.getLong("id");
        String name = jsonObject.getString("name");
        JSONArray charactersJson = jsonObject.optJSONArray("characters");
        VnaasCharacter[] characters = jsonArrayToCharacters(charactersJson);
        return new VnaasNovel(id, name, characters);
    }

    private VnaasCharacter jsonToCharacter(JSONObject jsonObject) throws JSONException {
        long id = jsonObject.getLong("id");
        String name = jsonObject.getString("name");
        JSONArray novelsJson = jsonObject.optJSONArray("novels");
        VnaasNovel[] novels = jsonArrayToNovels(novelsJson);
        return new VnaasCharacter(id, name, novels);
    }

    private VnaasCharacter[] jsonArrayToCharacters(JSONArray charactersJson) throws JSONException {
        if (charactersJson == null) {
            return null;
        }
        VnaasCharacter[] characters = new VnaasCharacter[charactersJson.length()];
        for (int i = 0; i < charactersJson.length(); ++i) {
            characters[i] = jsonToCharacter(charactersJson.getJSONObject(i));
        }
        return characters;
    }

    private VnaasNovel[] jsonArrayToNovels(JSONArray novelsJson) throws JSONException {
        if (novelsJson == null) {
            return null;
        }
        VnaasNovel[] novels = new VnaasNovel[novelsJson.length()];
        for (int i = 0; i < novelsJson.length(); ++i) {
            novels[i] = jsonToNovel(novelsJson.getJSONObject(i));
        }
        return novels;
    }

    private VnaasQuote jsonToQuote(JSONObject quoteJson) throws JSONException {
        if (quoteJson == null) {
            return null;
        }
        String text = quoteJson.getString("text");
        JSONObject characterJson = quoteJson.getJSONObject("character");
        VnaasCharacter character = jsonToCharacter(characterJson);
        JSONObject novelJson = quoteJson.getJSONObject("novel");
        VnaasNovel novel = jsonToNovel(novelJson);
        return new VnaasQuote(text, character, novel);
    }
}
