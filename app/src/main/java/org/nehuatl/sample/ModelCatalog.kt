package org.nehuatl.sample

data class ModelInfo(
    val id: String,
    val name: String,
    val url: String,
    val fileName: String,
    val description: String
)

object ModelCatalog {

    private const val BASE = "https://huggingface.co/bartowski/"

    val models: List<ModelInfo> = listOf(

        // 1. PHI-3.5-MINI — САМЫЙ УМНЫЙ В РАЗМЕРЕ
        ModelInfo(
            id = "phi35_mini",
            name = "Phi-3.5-mini (3.8B) — умник",
            url = BASE + "Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf",
            fileName = "phi35_mini_q4.gguf",
            description = "Самая умная модель в этом списке. 3.8B параметров, ~2.4 ГБ. Отлично следует инструкциям, сильна в логике, рассуждениях и коде. 128K контекст. Русский язык — средний, но понимает. Если телефон тянет 4+ ГБ RAM — это лучший выбор для сложных задач. [citation:6]"
        ),

        // 2. QWEN2.5-3B — БАЛАНС
        ModelInfo(
            id = "qwen25_3b",
            name = "Qwen2.5-3B — баланс",
            url = BASE + "Qwen2.5-3B-Instruct-GGUF/resolve/main/Qwen2.5-3B-Instruct-Q4_K_M.gguf",
            fileName = "qwen25_3b_q4.gguf",
            description = "Золотая середина. 3B параметров, ~1.9 ГБ. Умнее Gemma 2B, легче Phi-3.5. Хорошо держит диалог, понимает русский лучше среднего. Отличный выбор на каждый день. [citation:19]"
        ),

        // 3. GEMMA 2 2B — БЫСТРАЯ
        ModelInfo(
            id = "gemma2_2b",
            name = "Gemma 2 2B — быстрая",
            url = BASE + "gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
            fileName = "gemma2_2b_q4.gguf",
            description = "Лёгкая и шустрая. 2B параметров, ~1.7 ГБ. Google обучал её на качественных данных, поэтому для своего размера она удивительно толковая. Русский — слабее, чем у Qwen, но для английского идеальна. Быстрый отклик на слабых телефонах. [citation:5]"
        ),

        // 4. VIKHR-QWEN-2.5-0.5B — РУССКИЙ
        ModelInfo(
            id = "vikhr_qwen_05b",
            name = "Vikhr-Qwen 0.5B — русский",
            url = "https://huggingface.co/QuantFactory/Vikhr-Qwen-2.5-0.5b-Instruct-GGUF/resolve/main/Vikhr-Qwen-2.5-0.5b-Instruct.Q4_K_M.gguf",
            fileName = "vikhr_qwen_05b_q4.gguf",
            description = "Специально дообучена на русском (датасет GrandMaster-PRO-MAX, 150k инструкций). 0.5B, ~0.4 ГБ. Очень лёгкая, летает на любом телефоне. В 4 раза эффективнее базовой Qwen. Для простых команд, заметок, диалога на русском — идеальна. Не жди от неё сложных рассуждений. [citation:8]"
        ),

        // 5. VIKHR-LLAMA-3.2-1B — РУССКИЙ + LLAMA
        ModelInfo(
            id = "vikhr_llama_1b",
            name = "Vikhr-Llama 3.2 1B — русский",
            url = "https://huggingface.co/QuantFactory/Vikhr-Llama-3.2-1B-Instruct-GGUF/resolve/main/Vikhr-Llama-3.2-1B-Instruct.Q4_K_M.gguf",
            fileName = "vikhr_llama_1b_q4.gguf",
            description = "Llama 3.2 1B, дообученная на русском. ~0.8 ГБ. В 5 раз эффективнее базовой. Хороший компромисс: чуть умнее Vikhr-Qwen 0.5B, но всё ещё очень лёгкая. Понимает русский, держит контекст диалога. [citation:15]"
        ),

        // 6. LLAMA 3.2 1B — МОБИЛЬНАЯ
        ModelInfo(
            id = "llama32_1b",
            name = "Llama 3.2 1B — мобильная",
            url = "https://huggingface.co/dispatchAI/Llama-3.2-1B-Instruct-Q4-mobile/resolve/main/ggml-model-Q4_K_M.gguf",
            fileName = "llama32_1b_q4.gguf",
            description = "Оптимизирована под мобильные (Snapdragon 865+). 1.23B, всего ~767 МБ. Скорость ~28 токенов/с на CPU, память ~1.2 ГБ. Качество ~95% от FP16. Идеальна, если телефон слабый, но хочется нормальный английский. [citation:17]"
        ),

        // 7. SMOLM2 1.7B — ДИАЛОГ
        ModelInfo(
            id = "smollm2_17b",
            name = "SmolLM2 1.7B — диалог",
            url = BASE + "SmolLM2-1.7B-Instruct-GGUF/resolve/main/SmolLM2-1.7B-Instruct-Q4_K_M.gguf",
            fileName = "smollm2_17b_q4.gguf",
            description = "HuggingFace обучали её специально для диалогов. 1.7B, ~1.1 ГБ. Дружелюбная, хорошо держит беседу. Английский — отлично, русский — слабо. Хороша для теста «характера» модели. [citation:9]"
        ),

        // 8. DOLPHIN 3.0 LLAMA 3.2 3B — БЕЗ ЦЕНЗУРЫ
        ModelInfo(
            id = "dolphin3_llama3b",
            name = "Dolphin 3.0 Llama 3.2 3B — без цензуры",
            url = BASE + "Dolphin3.0-Llama3.2-3B-GGUF/resolve/main/Dolphin3.0-Llama3.2-3B-Q4_K_M.gguf",
            fileName = "dolphin3_llama3b_q4.gguf",
            description = "Llama 3.2 3B, дообученная без цензуры (Dolphin). ~2.0 ГБ. Отвечает на всё, не отказывается. Ум на уровне Qwen2.5-3B. Английский — отлично, русский — средне. Интересно для теста «свободного» поведения. [citation:9]"
        ),

        // 9. QWEN2.5-0.5B — СОВСЕМ ЛЁГКАЯ
        ModelInfo(
            id = "qwen25_05b",
            name = "Qwen2.5 0.5B — минимальная",
            url = BASE + "Qwen2.5-0.5B-Instruct-GGUF/resolve/main/Qwen2.5-0.5B-Instruct-Q4_K_M.gguf",
            fileName = "qwen25_05b_q4.gguf",
            description = "Базовая Qwen2.5 на 0.5B, ~0.4 ГБ. Очень слабая, но показывает «нижнюю границу»: что вообще способна выдать модель такого размера. Полезна для сравнения с Vikhr-Qwen (которая на той же базе, но дообучена). [citation:10]"
        ),

        // 10. PHI-3.5-MINI UNCENSORED — УМ + БЕЗ ЦЕНЗУРЫ
        ModelInfo(
            id = "phi35_uncensored",
            name = "Phi-3.5-mini Uncensored — ум + свобода",
            url = BASE + "Phi-3.5-mini-instruct_Uncensored-GGUF/resolve/main/Phi-3.5-mini-instruct_Uncensored-Q4_K_M.gguf",
            fileName = "phi35_uncensored_q4.gguf",
            description = "Тот же Phi-3.5-mini (самый умный в списке), но снята цензура. ~2.4 ГБ. Для тех случаев, когда стандартный Phi отказывается отвечать, а ответ нужен. Русский — как у оригинала (средний). [citation:13]"
        )
    )
}
