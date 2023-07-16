package com.crossbowffs.quotelock.app.configs.openai

object OpenAIPrefKeys {
    const val PREF_OPENAI = "openai"

    const val PREF_OPENAI_LANGUAGE = "pref_openai_language"
    const val PREF_OPENAI_MODEL = "pref_openai_model"
    const val PREF_OPENAI_QUOTE_TYPE = "pref_openai_quote_type"
    const val PREF_OPENAI_API_KEY = "pref_openai_api_key"
    const val PREF_OPENAI_API_HOST = "pref_openai_host"

    const val PREF_OPENAI_QUOTE_TYPE_AI_GENERATED = 0
    const val PREF_OPENAI_QUOTE_TYPE_SYSTEM_GENERATED = 1

    const val PREF_OPENAI_LANGUAGE_DEFAULT = "English"
    const val PREF_OPENAI_MODEL_DEFAULT = "gpt-3.5-turbo"
    const val PREF_OPENAI_QUOTE_TYPE_DEFAULT = PREF_OPENAI_QUOTE_TYPE_AI_GENERATED
    const val PREF_OPENAI_API_HOST_DEFAULT = "https://api.openai.com"
    const val PREF_OPENAI_CHAT_API_PATH = "/v1/chat/completions"
    const val PREF_OPENAI_CHAT_SUB_PATH = "dashboard/billing/subscription"
    const val PREF_OPENAI_CHAT_USAGE_PATH = "dashboard/billing/usage"

    const val PREF_OPENAI_API_KEY_SUPPORT_LINK = "https://platform.openai.com/account/api-keys"
    const val PREF_OPENAI_API_KEY_PREFIX = "sk-"
    const val PREF_PASSWORD_MASK = '\u2022'

    private const val PREF_OPENAI_QUOTE_AI_GENERATED_SYSTEM_PROMPT = """
        You are a literary assistant who is well-versed in literature works from various countries and languages. Users will input a language name and you will create a creative and philosophical aphorism that has never appeared in literary works in the language. Set the style of aphorism to be the most representative of the cultural style of the language region. The output should include details such as the content of the aphorism and its category. Please provide the information in a JSON format where the keys are: "quote" for the aphorism and "category" for the category. All values should be presented in the language of the quote. If the value is unknown or empty, please set the value to an empty string("").
    """

    private const val PREF_OPENAI_QUOTE_EXISTING_SYSTEM_PROMPT = """
        You are a search assistant designed to familiarize yourself with literary works from various countries. Users will input a language name and you will generate a random quote in the language. Set the style of quote to be the most representative of the cultural style of the language region. The output should include details such as the content of the quote, its author, and its category. Please provide the information in a JSON format where the keys are: "quote" for the quote content, "source" for the source of the quote, and "category" for the category. All values should be presented in the language of the quote. If the value is unknown or empty, please set the value to an empty string("").
    """

    val PREF_OPENAI_QUOTE_SYSTEM_PROMPT_DEFAULT =
        PREF_OPENAI_QUOTE_AI_GENERATED_SYSTEM_PROMPT.trimIndent()
    val PREF_OPENAI_QUOTE_SYSTEM_PROMPTS = mapOf(
        PREF_OPENAI_QUOTE_TYPE_AI_GENERATED to PREF_OPENAI_QUOTE_AI_GENERATED_SYSTEM_PROMPT.trimIndent(),
        PREF_OPENAI_QUOTE_TYPE_SYSTEM_GENERATED to PREF_OPENAI_QUOTE_EXISTING_SYSTEM_PROMPT.trimIndent(),
    )
}