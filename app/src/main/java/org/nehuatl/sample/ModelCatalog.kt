package org.nehuatl.sample

data class ModelInfo(
    val id: String,
    val name: String,
    val url: String,
    val fileName: String,
    val description: String
)

object ModelCatalog {

    private const val PARABLE_URL =
        "https://huggingface.co/AnkitAI/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF/resolve/main/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF-Q6_K.gguf"

    private const val PARABLE_DESCRIPTION =
        "Parable-Granite-3B v2 — модель на базе IBM Granite 4.1 3B, дообученная на реальных траекториях диалогов Claude Fable 5 и GPT-5.5. Запускается примерно на 3 ГБ памяти, Q4-квантование работает даже на устройствах уровня Raspberry Pi. Перед ответом модель сначала размышляет — использует фазу рассуждения <think>. Сильна в объяснениях, идиомах и однострочных командах."

    val models: List<ModelInfo> = listOf(
        ModelInfo(
            id = "model_01",
            name = "Parable-Granite 3B (тест 1)",
            url = PARABLE_URL,
            fileName = "parable_granite_01.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_02",
            name = "Parable-Granite 3B (тест 2)",
            url = PARABLE_URL,
            fileName = "parable_granite_02.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_03",
            name = "Parable-Granite 3B (тест 3)",
            url = PARABLE_URL,
            fileName = "parable_granite_03.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_04",
            name = "Parable-Granite 3B (тест 4)",
            url = PARABLE_URL,
            fileName = "parable_granite_04.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_05",
            name = "Parable-Granite 3B (тест 5)",
            url = PARABLE_URL,
            fileName = "parable_granite_05.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_06",
            name = "Parable-Granite 3B (тест 6)",
            url = PARABLE_URL,
            fileName = "parable_granite_06.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_07",
            name = "Parable-Granite 3B (тест 7)",
            url = PARABLE_URL,
            fileName = "parable_granite_07.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_08",
            name = "Parable-Granite 3B (тест 8)",
            url = PARABLE_URL,
            fileName = "parable_granite_08.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_09",
            name = "Parable-Granite 3B (тест 9)",
            url = PARABLE_URL,
            fileName = "parable_granite_09.gguf",
            description = PARABLE_DESCRIPTION
        ),
        ModelInfo(
            id = "model_10",
            name = "Parable-Granite 3B (тест 10)",
            url = PARABLE_URL,
            fileName = "parable_granite_10.gguf",
            description = PARABLE_DESCRIPTION
        )
    )
}
