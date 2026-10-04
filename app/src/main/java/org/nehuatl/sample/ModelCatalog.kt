package org.nehuatl.sample

data class ModelInfo(
    val id: String,
    val name: String,
    val url: String,
    val fileName: String,
    val description: String,
    val mmprojUrl: String? = null,
    val mmprojFileName: String? = null
)

object ModelCatalog {

    private const val BART = "https://huggingface.co/bartowski/"
    private const val PRITHIV = "https://huggingface.co/prithivMLmods/"

    val models: List<ModelInfo> = listOf(

        // ===== 1. VIBETHINKER-3B — ГЛАВНАЯ ДУМАЮЩАЯ =====
        ModelInfo(
            id = "vibethinker_3b",
            name = "VibeThinker-3B — видно как думает ⭐",
            url = PRITHIV + "VibeThinker-3B-GGUF/resolve/main/VibeThinker-3B.Q4_K_M.gguf",
            fileName = "vibethinker_3b_q4.gguf",
            description = "Та самая VibeThinker. 3B, ~1.93 ГБ в Q4_K_M. Построена на Qwen2.5-Coder-3B, обучена на верифицируемых рассуждениях (математика, код, STEM). На IMO-AnswerBench набрала 76.4 — уровень DeepSeek V3.2 (671B). Ты видишь её цепочку размышлений. 96.1% прохождение LeetCode. Рекомендуется ставить maxTokens = 60000–100000, иначе не увидишь всё размышление. Русский язык — не заявлен. [citation:1][citation:7][citation:13]"
        ),

        // ===== 2. QWEN3-4B THINKING — ДУМАЮЩАЯ, RUS =====
        ModelInfo(
            id = "qwen3_4b_thinking",
            name = "Qwen3-4B Thinking — думающая, русский",
            url = PRITHIV + "Qwen3-4B-Instruct-2507-GGUF/resolve/main/Qwen3-4B-Thinking-2507.Q4_K_M.gguf",
            fileName = "qwen3_4b_thinking_q4.gguf",
            description = "Qwen3-4B в режиме thinking (2507). 4B, ~2.5 ГБ. Официальная Qwen с режимом размышлений. Русский язык — хорошо. Умная, следует инструкциям. Если VibeThinker заточена под код и математику, эта — универсальнее. Контекст 32K. [citation:4]"
        ),

        // ===== 3. QWEN3-4B KIMI REASONING DISTILLED — ДУМАЮЩАЯ #3 =====
        ModelInfo(
            id = "qwen3_kimi_reasoning",
            name = "Qwen3-4B Kimi Reasoning — думающая",
            url = "https://huggingface.co/khazarai/Qwen3-4B-Kimi2.5-Reasoning-Distilled-GGUF/resolve/main/qwen3-4b-thinking-2507.Q4_K_M.gguf",
            fileName = "qwen3_kimi_reasoning_q4.gguf",
            description = "Qwen3-4B, дистиллированная с Kimi-2.5-thinking. ~2.5 ГБ. Обучена на длинных цепочках рассуждений Kimi. Разбивает задачи, самокорректируется, даёт аналитические ответы. Третья думающая модель в списке — если две предыдущие не подошли, попробуй эту. [citation:10]"
        ),

        // ===== 4. GEMMA 3 4B — RUS + 128K + VISION =====
        ModelInfo(
            id = "gemma3_4b",
            name = "Gemma 3 4B — русский, 128K, картинки",
            url = BART + "google_gemma-3-4b-it-GGUF/resolve/main/google_gemma-3-4b-it-Q4_K_M.gguf",
            fileName = "gemma3_4b_q4.gguf",
            mmprojUrl = BART + "google_gemma-3-4b-it-GGUF/resolve/main/mmproj-google_gemma-3-4b-it-f16.gguf",
            mmprojFileName = "gemma3_4b_mmproj_f16.gguf",
            description = "Gemma 3 от Google. 4B, ~2.3 ГБ + ~850 МБ проектор. 128K контекст, 140+ языков (русский — отлично). Понимает картинки. Для анализа изображений загружай оба файла. Лучшая русскоязычная модель в списке для общих задач. [citation:5][citation:16]"
        ),

        // ===== 5. GRANITE 4.1 3B — IBM, 128K, RUS =====
        ModelInfo(
            id = "granite_3b",
            name = "Granite 4.1 3B — IBM, 128K, русский",
            url = BART + "ibm-granite_granite-4.1-3b-GGUF/resolve/main/granite-4.1-3b-Q4_K_M.gguf",
            fileName = "granite_3b_q4.gguf",
            description = "IBM Granite 4.1 на 3B, ~2.17 ГБ. 128K контекст. Корпоративная надёжность, чистая речь, хорошее следование инструкциям. Русский язык — хороший (IBM обучает на многих языках). Отлично для структурированных ответов и документов. "
        ),

        // ===== 6. PHI-4-MINI — MICROSOFT, 128K, ЛОГИКА =====
        ModelInfo(
            id = "phi4_mini",
            name = "Phi-4-mini — Microsoft, 128K, логика",
            url = "https://huggingface.co/jc-builds/Phi-4-mini-instruct-GGUF/resolve/main/Phi-4-mini-instruct-Q4_K_M.gguf",
            fileName = "phi4_mini_q4.gguf",
            description = "Phi-4-mini от Microsoft. 3.8B, ~2.3 ГБ. 128K контекст (YaRN). Бьёт модели в 5–10 раз больше на MATH и GPQA. MIT-лицензия. Лучший выбор для сложной логики, математики, структурированных рассуждений. Русский — средний, но понимает. [citation:8]"
        ),

        // ===== 7. SMOLLM3 3B — ДИАЛОГ, 128K =====
        ModelInfo(
            id = "smollm3_3b",
            name = "SmolLM3 3B — диалог, гибрид",
            url = BART + "HuggingFaceTB_SmolLM3-3B-GGUF/resolve/main/SmolLM3-3B-Q4_K_M.gguf",
            fileName = "smollm3_3b_q4.gguf",
            description = "SmolLM3 от HuggingFace. 3B, ~1.92 ГБ. 128K контекст. Гибридная: может думать или просто отвечать. 6 языков (русского нет). Дружелюбная, хороша для диалога. "
        ),

        // ===== 8. MINICPM5-2B — 131K, ЛЁГКАЯ =====
        ModelInfo(
            id = "minicpm5_2b",
            name = "MiniCPM5-2B — лёгкая, 131K",
            url = "https://huggingface.co/saidutta69/MiniCPM5-2B-GGUF/resolve/main/MiniCPM5-2B-Q4_K_M.gguf",
            fileName = "minicpm5_2b_q4.gguf",
            description = "MiniCPM5-2B, ~1.56 ГБ. 131K контекст! Огромный контекст при малом размере. EN/ZH двуязычная, нативная поддержка tool calling. Русского нет, но для английского — отлично. Быстрая даже на CPU. [citation:14]"
        ),

        // ===== 9. QWEN3.8-2B — 262K, ДУМАЮЩАЯ =====
        ModelInfo(
            id = "qwen38_2b",
            name = "Qwen3.8-2B — 262K, думающая",
            url = "https://huggingface.co/empero-ai/Qwen3.8-2B-GGUF/resolve/main/Qwen3.8-2B-Q4_K_M.gguf",
            fileName = "qwen38_2b_q4.gguf",
            description = "Qwen3.8-2B, ~1.3 ГБ в Q4_K_M. 262K контекст! Дистиллирована с Qwen3.8 2.4T. Режим размышлений, function calling. Огромный контекст за минимальный размер. Русский — как у Qwen (средне-хороший). [citation:18]"
        ),

        // ===== 10. VIBETHINKER-3B HERETIC — БЕЗ ЦЕНЗУРЫ =====
        ModelInfo(
            id = "vibethinker_heretic",
            name = "VibeThinker-3B Heretic — без цензуры",
            url = "https://huggingface.co/saidutta69/VibeThinker-3B-heretic/resolve/main/VibeThinker-3B-heretic-Q4_K_M.gguf",
            fileName = "vibethinker_heretic_q4.gguf",
            description = "Та же VibeThinker-3B, но снята цензура (abliterated). ~1.80 ГБ в Q4_K_M. Сохранены все способности к рассуждениям и код. Не отказывается отвечать. Отлично для тестов «свободного» поведения. Не заставляет модель говорить с собой — это не «сырая» модель. [citation:9]"
        ),

        // ===== 11. LLAMA 3.2 1B HERETIC — ЛЁГКАЯ, БЕЗ ЦЕНЗУРЫ =====
        ModelInfo(
            id = "llama32_1b_heretic",
            name = "Llama 3.2 1B Heretic — лёгкая, без цензуры",
            url = "https://huggingface.co/Green-Eye/Llama-3.2-1B-Instruct-heretic/resolve/main/Llama-3.2-1B-Instruct-heretic-Q4_K_M.gguf",
            fileName = "llama32_1b_heretic_q4.gguf",
            description = "Llama 3.2 1B с снятой цензурой. ~0.81 ГБ. Отказы упали с 96/100 до 7/100. Знания и следование инструкциям почти не пострадали. Идеальна для слабых телефонов, когда нужна «свободная» модель. Русский — слабый. [citation:3]"
        ),

        // ===== 12. NOVA-LFM 1.2B — ДУМАЮЩАЯ, ЛЁГКАЯ =====
        ModelInfo(
            id = "nova_lfm_12b",
            name = "Nova-LFM 1.2B Thinking — думающая, лёгкая",
            url = "https://huggingface.co/NovachronoAI/Nova-LFM-1.2B-Thinking-GGUF/resolve/main/Nova-LFM-1.2B-Thinking-Q4_K_M.gguf",
            fileName = "nova_lfm_12b_q4.gguf",
            description = "Nova-LFM 1.2B Thinking, ~0.7 ГБ. «System 2 Thinking»: останавливается, проверяет логику, исправляет ошибки. GSM8K 53.5% — лучше Llama 3.2 1B и Gemma 2 2B. Думающая, но очень лёгкая. Идеальна для слабых телефонов. Английский. [citation:2]"
        ),

        // ===== 13. GEMMA 2 2B — СЛАБАЯ, ДЛЯ СРАВНЕНИЯ =====
        ModelInfo(
            id = "gemma2_2b",
            name = "Gemma 2 2B — слабая (как сейчас)",
            url = BART + "gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
            fileName = "gemma2_2b_q4.gguf",
            description = "Оставляем как эталон слабой модели. 2B, ~1.7 ГБ. Контекст 8K — мало для «вспомни». Быстрая, но не умная. Для сравнения. "
        ),

        // ===== 14. VIKHR-QWEN 0.5B — РУССКИЙ, СЛАБАЯ =====
        ModelInfo(
            id = "vikhr_qwen_05b",
            name = "Vikhr-Qwen 0.5B — русский, слабая",
            url = "https://huggingface.co/QuantFactory/Vikhr-Qwen-2.5-0.5b-Instruct-GGUF/resolve/main/Vikhr-Qwen-2.5-0.5b-Instruct.Q4_K_M.gguf",
            fileName = "vikhr_qwen_05b_q4.gguf",
            description = "Русская, 0.5B, ~0.4 ГБ. Очень лёгкая. Для простых команд и заметок. Слабая, но говорит по-русски. "
        ),

                // ===== 15. QWEN2.5-3B — УНИВЕРСАЛЬНАЯ (БАЗА VIBETHINKER) =====
        ModelInfo(
            id = "qwen25_3b",
            name = "Qwen2.5-3B — универсальная база",
            url = BART + "Qwen2.5-3B-Instruct-GGUF/resolve/main/Qwen2.5-3B-Instruct-Q4_K_M.gguf",
            fileName = "qwen25_3b_q4.gguf",
            description = "Базовая Qwen2.5-3B, ~1.9 ГБ. На её основе сделана VibeThinker. Универсальная, без режима «размышлений». Хороший русский, диалог, следование инструкциям. Для тех, кому не нужен thinking. "
        ),

        // ===== 16. PARABLE-GRANITE HERETIC — БЕЗ ЦЕНЗУРЫ + THINKING =====
        ModelInfo(
            id = "parable_granite_heretic",
            name = "Parable-Granite 3B Heretic — без цензуры, thinking",
            url = "https://huggingface.co/saidutta69/Parable-Granite-4.1-3B-Claude-Fable-5-heretic/resolve/main/Parable-Granite-4.1-3B-Claude-Fable-5-heretic-Q4_K_M.gguf",
            fileName = "parable_granite_heretic_q4.gguf",
            description = "Parable-Granite 3B с снятой цензурой (abliterated). Q4_K_M, ~2.1 ГБ. Отказы упали с 96/100 до 5/100, способности к рассуждениям и коду сохранены. Режим размышлений <think> работает. Для тех случаев, когда обычная Parable отказывается, а ответ нужен. "
        ),

        // ===== 17. NVIDIA NEMOTRON-3-NANO-4B — ОТ NVIDIA, MoE =====
        ModelInfo(
            id = "nemotron_3_nano_4b",
            name = "NVIDIA Nemotron-3-Nano-4B — от NVIDIA, MoE",
            url = "https://huggingface.co/nvidia/NVIDIA-Nemotron-3-Nano-4B-BF16/resolve/main/NVIDIA-Nemotron3-Nano-4B-Q4_K_M.gguf",
            fileName = "nemotron_3_nano_4b_q4.gguf",
            description = "NVIDIA Nemotron-3-Nano-4B — модель от мирового лидера, заточена под edge-устройства (NPC, ассистенты, IoT). Q4_K_M, ~2.84 ГБ. Архитектура MoE. Русский язык входит в обучающий корпус (15 языков). Отличная скорость на реальном железе. "
        ),

        // ===== 18. QWEN3.5-4B — 262K КОНТЕКСТ =====
        ModelInfo(
            id = "qwen35_4b",
            name = "Qwen3.5-4B — 262K контекст",
            url = "https://huggingface.co/unsloth/Qwen3.5-4B-GGUF/resolve/main/Qwen3.5-4B-Q4_K_S.gguf",
            fileName = "qwen35_4b_q4.gguf",
            description = "Qwen3.5-4B от Alibaba, Q4_K_S, ~2.59 ГБ. Контекст 262144 токена (262K) — огромный. Режим размышлений, function calling, нативное vision (нужен mmproj — не входит). Русский язык — слабый (низкий балл MERA), для английского и мультиязычных задач — отлично. "
        ),

                // ===== 19. QVIKHR-3-4B — РУССКАЯ ОТ VIKHR =====
        ModelInfo(
            id = "qvikhr_3_4b",
            name = "QVikhr-3-4B — русская, Qwen3-4B база",
            url = "https://huggingface.co/prithivMLmods/QVikhr-3-4B-it-F32-GGUF/resolve/main/QVikhr-3-4B-Instruction.Q4_K_M.gguf",
            fileName = "qvikhr_3_4b_q4.gguf",
            description = "Русская модель от команды Vikhr на базе Qwen3-4B. Q4_K_M, ~2.5 ГБ. Обучена на датасете GrandMaster2. Ru Arena General = 78.2 — значительно выше базовой Qwen3-4B (64.8). Лучший выбор для русского языка среди моделей этого размера. "
        ),

        // ===== 20. HY-MT2-1.8B — ПЕРЕВОДЧИК, 33 ЯЗЫКА =====
        ModelInfo(
            id = "hy_mt2_1_8b",
            name = "Hy-MT2-1.8B — переводчик, 33 языка",
            url = "https://huggingface.co/tencent/Hy-MT2-1.8B-GGUF/resolve/main/Hy-MT2-1.8B-Q4_K_M.gguf",
            fileName = "hy_mt2_1_8b_q4.gguf",
            description = "Модель-ПЕРЕВОДЧИК от Tencent, а не диалоговая. Q4_K_M, ~1.13 ГБ. 33 языка, включая русский. Создана специально для телефонов — 1.25-bit версия весит всего 440 МБ. Обгоняет Microsoft Translator и Doubao. ВАЖНО: у модели нет промпта по умолчанию — для перевода нужно задать промпт вручную: «Переведи следующий текст на русский, без дополнительных объяснений:». Модель НЕ ведёт диалог — только переводит. "
        )
    )
}
