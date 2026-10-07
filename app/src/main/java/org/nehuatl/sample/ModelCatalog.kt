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
            description = "Та самая VibeThinker. 3B, ~1.93 ГБ в Q4_K_M. Построена на Qwen2.5-Coder-3B, обучена на верифицируемых рассуждениях (математика, код, STEM). На IMO-AnswerBench набрала 76.4 — уровень DeepSeek V3.2 (671B). Ты видишь её цепочку размышлений. 96.1% прохождение LeetCode. Рекомендуется ставить maxTokens = 60000–100000, иначе не увидишь всё размышление. Русский язык — не заявлен. "
        ),

        // ===== 2. QWEN3-4B INSTRUCT — УНИВЕРСАЛЬНАЯ, RUS =====
        ModelInfo(
            id = "qwen3_4b_instruct",
            name = "Qwen3-4B Instruct — универсальная, русский",
            url = PRITHIV + "Qwen3-4B-Instruct-2507-GGUF/resolve/main/Qwen3-4B-Instruct-2507.Q4_K_M.gguf",
            fileName = "qwen3_4b_instruct_q4.gguf",
            description = "Qwen3-4B Instruct (2507) — официальная Qwen без режима thinking. 4B, ~2.5 ГБ. Универсальная: диалог, следование инструкциям, русский язык — хорошо. Контекст 32K. Для тех, кому не нужны долгие размышления, а нужен быстрый ответ. "
        ),

        // ===== 3. MEDPSY-4B — МЕДИЦИНА (EN) =====
        ModelInfo(
            id = "medpsy_4b",
            name = "MedPsy-4B — медицина (English)",
            url = "https://huggingface.co/qvac/MedPsy-4B-GGUF/resolve/main/medpsy-4b-q4_k_m-imat.gguf",
            fileName = "medpsy_4b_q4.gguf",
            description = "Медицинская модель от Tether на базе Qwen3-4B-Thinking. Q4_K_M ~2.72 ГБ. Reasoning-модель: показывает цепочку размышлений. HealthBench Hard = 56 — обгоняет Google MedGemma-27B. Медицина, химия, биология. ⚠️ ТОЛЬКО АНГЛИЙСКИЙ — задавай вопросы через переводчик Hy-MT2. ⚠️ Требует 4+ ГБ RAM. Не专门 по травам и грибам. "
        ),

        // ===== 4. GEMMA 3 4B — RUS + 128K + VISION (Q6_K) =====
        ModelInfo(
            id = "gemma3_4b",
            name = "Gemma 3 4B — русский, 128K, картинки",
            url = BART + "google_gemma-3-4b-it-GGUF/resolve/main/google_gemma-3-4b-it-Q6_K.gguf",
            fileName = "gemma3_4b_q6.gguf",
            mmprojUrl = BART + "google_gemma-3-4b-it-GGUF/resolve/main/mmproj-google_gemma-3-4b-it-f16.gguf",
            mmprojFileName = "gemma3_4b_mmproj_f16.gguf",
            description = "Gemma 3 от Google. 4B, Q6_K ~3.0 ГБ + ~850 МБ проектор (итого ~3.85 ГБ). 128K контекст, 140+ языков (русский — отлично). Понимает картинки. Для анализа изображений загружай оба файла. ВАЖНО: Q6_K впритык к лимиту 3 ГБ — если не грузится, переключись на Q5_K_M вручную. "
        ),

        // ===== 5. PARABLE-GRANITE 3B — IBM Granite база =====
        ModelInfo(
            id = "parable_granite",
            name = "Parable-Granite 3B — умный, русский",
            url = "https://huggingface.co/AnkitAI/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF/resolve/main/Parable-Granite-4.1-3B-Claude-Fable-5-GGUF-Q4_K_M.gguf",
            fileName = "parable_granite_q4.gguf",
            description = "Parable-Granite 3B на базе IBM Granite 4.1, дообученная на траекториях Claude Fable 5 и GPT-5.5. Q4_K_M ~2 ГБ. Запускается на 3 ГБ RAM. Режим размышлений <think>. Сильна в объяснениях, идиомах, однострочных командах. Русский язык — понимает. "
        ),

        // ===== 6. PHI-4-MINI — MICROSOFT, 128K, ЛОГИКА (Q5_K_M) =====
        ModelInfo(
            id = "phi4_mini",
            name = "Phi-4-mini — Microsoft, 128K, логика",
            url = "https://huggingface.co/AtomicChat/Phi-4-mini-instruct-GGUF/resolve/main/Phi-4-mini-instruct-Q5_K_M.gguf",
            fileName = "phi4_mini_q5.gguf",
            description = "Phi-4-mini от Microsoft. 3.8B, Q5_K_M ~2.8 ГБ. 128K контекст (131072 токена). Бьёт модели в 5–10 раз больше на MATH и GPQA. MIT-лицензия. Лучший выбор для сложной логики, математики, структурированных рассуждений. Русский — средний, но понимает. "
        ),

        // ===== 7. TUTORAI-CHEMISTRY-PHI4 — ХИМИЯ (EN) =====
        ModelInfo(
            id = "tutorai_chemistry",
            name = "TutorAI-Chemistry-Phi4 — химия (English)",
            url = "https://huggingface.co/mradermacher/TutorAI-Chemistry-Phi4-GGUF/resolve/main/TutorAI-Chemistry-Phi4.Q4_K_M.gguf",
            fileName = "tutorai_chemistry_q4.gguf",
            description = "Химический репетитор на базе Phi-4-mini (3.8B), дообучен через RL (GRPO) специально для химии. Q4_K_M ~2.6 ГБ. Разбирает молекулярные взаимодействия, стехиометрию, формулы пошагово. Показывает цепочку размышлений <think>, финальный ответ в \\boxed{}. ⚠️ ТОЛЬКО АНГЛИЙСКИЙ — задавай вопросы через переводчик Hy-MT2. "
        ),

        // ===== 8. MINICPM5-2B — 131K, ЛЁГКАЯ (Q6_K) =====
        ModelInfo(
            id = "minicpm5_2b",
            name = "MiniCPM5-2B — лёгкая, 131K",
            url = "https://huggingface.co/prithivMLmods/MiniCPM5-2B-GGUF/resolve/main/MiniCPM5-2B.Q6_K.gguf",
            fileName = "minicpm5_2b_q6.gguf",
            description = "MiniCPM5-2B от OpenBMB, Q6_K ~2.0 ГБ. 131K контекст! Огромный контекст при малом размере. EN/ZH двуязычная, нативная поддержка tool calling. Русского нет, но для английского — отлично. Быстрая даже на CPU. "
        ),

        // ===== 9. QWEN3.8-2B — 262K, ДУМАЮЩАЯ (Q5_K_M) =====
        ModelInfo(
            id = "qwen38_2b",
            name = "Qwen3.8-2B — 262K, думающая",
            url = "https://huggingface.co/empero-ai/Qwen3.8-2B-Distill-GGUF/resolve/main/Qwen3.8-2B-Q5_K_M.gguf",
            fileName = "qwen38_2b_q5.gguf",
            description = "Qwen3.8-2B, Q5_K_M ~1.46 ГБ. 262K контекст! Дистиллирована с Qwen3.8 2.4T. Режим размышлений, function calling. Огромный контекст за минимальный размер. Русский — как у Qwen (средне-хороший). "
        ),

        // ===== 10. VIBETHINKER-3B HERETIC — БЕЗ ЦЕНЗУРЫ (Q5_K_M) =====
        ModelInfo(
            id = "vibethinker_heretic",
            name = "VibeThinker-3B Heretic — без цензуры",
            url = "https://huggingface.co/saidutta69/VibeThinker-3B-heretic/resolve/main/VibeThinker-3B-heretic-Q5_K_M.gguf",
            fileName = "vibethinker_heretic_q5.gguf",
            description = "Та же VibeThinker-3B, но снята цензура (abliterated). Q5_K_M ~2.4 ГБ. Сохранены все способности к рассуждениям и код. Не отказывается отвечать. Отлично для тестов «свободного» поведения. Не заставляет модель говорить с собой — это не «сырая» модель. "
        ),

        // ===== 11. LLAMA 3.2 1B HERETIC — ЛЁГКАЯ, БЕЗ ЦЕНЗУРЫ (Q8_0) =====
        ModelInfo(
            id = "llama32_1b_heretic",
            name = "Llama 3.2 1B Heretic — лёгкая, без цензуры",
            url = "https://huggingface.co/saidutta69/Llama-3.2-1B-Instruct-heretic/resolve/main/llama3.2-1b-Q8_0.gguf",
            fileName = "llama32_1b_heretic_q8.gguf",
            description = "Llama 3.2 1B с снятой цензурой. Q8_0 ~1.23 ГБ. Отказы упали с 96/100 до 7/100. Знания и следование инструкциям почти не пострадали. Идеальна для слабых телефонов, когда нужна «свободная» модель. Русский — слабый. "
        ),

        // ===== 12. NOVA-LFM 1.2B — ДУМАЮЩАЯ, ЛЁГКАЯ (Q5_K_M) =====
        ModelInfo(
            id = "nova_lfm_12b",
            name = "Nova-LFM 1.2B Thinking — думающая, лёгкая",
            url = "https://huggingface.co/mradermacher/Nova-LFM-1.2B-Thinking-i1-GGUF/resolve/main/Nova-LFM-1.2B-Thinking.i1-Q5_K_M.gguf",
            fileName = "nova_lfm_12b_q5.gguf",
            description = "Nova-LFM 1.2B Thinking, Q5_K_M ~0.9 ГБ. «System 2 Thinking»: останавливается, проверяет логику, исправляет ошибки. GSM8K 53.5% — лучше Llama 3.2 1B и Gemma 2 2B. Думающая, но очень лёгкая. Идеальна для слабых телефонов. Английский. "
        ),

        // ===== 13. GEMMA 2 2B — СЛАБАЯ, ДЛЯ СРАВНЕНИЯ (Q6_K) =====
        ModelInfo(
            id = "gemma2_2b",
            name = "Gemma 2 2B — слабая (как сейчас)",
            url = BART + "gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q6_K.gguf",
            fileName = "gemma2_2b_q6.gguf",
            description = "Оставляем как эталон слабой модели. 2B, Q6_K ~2.1 ГБ. Контекст 8K — мало для «вспомни». Быстрая, но не умная. Для сравнения. "
        ),

                // ===== 14. VIKHR-QWEN 0.5B — РУССКИЙ, СЛАБАЯ (Q8_0) =====
        ModelInfo(
            id = "vikhr_qwen_05b",
            name = "Vikhr-Qwen 0.5B — русский, слабая",
            url = "https://huggingface.co/Vikhrmodels/Vikhr-Qwen-2.5-0.5B-instruct-GGUF/resolve/main/Vikhr-Qwen-2.5-0.5B-instruct-Q8_0.gguf",
            fileName = "vikhr_qwen_05b_q8.gguf",
            description = "Русская, 0.5B, Q8_0 ~0.5 ГБ. Очень лёгкая. Для простых команд и заметок. Слабая, но говорит по-русски. Официальный GGUF от Vikhrmodels. "
        ),

        // ===== 15. QWEN2.5-3B — УНИВЕРСАЛЬНАЯ (Q6_K) =====
        ModelInfo(
            id = "qwen25_3b",
            name = "Qwen2.5-3B — универсальная база",
            url = BART + "Qwen2.5-3B-Instruct-GGUF/resolve/main/Qwen2.5-3B-Instruct-Q6_K.gguf",
            fileName = "qwen25_3b_q6.gguf",
            description = "Базовая Qwen2.5-3B, Q6_K ~2.6 ГБ. На её основе сделана VibeThinker. Универсальная, без режима «размышлений». Хороший русский, диалог, следование инструкциям. Для тех, кому не нужен thinking. "
        ),

        // ===== 16. PARABLE-GRANITE HERETIC — БЕЗ ЦЕНЗУРЫ + THINKING (Q5_K_M) =====
        ModelInfo(
            id = "parable_granite_heretic",
            name = "Parable-Granite 3B Heretic — без цензуры, thinking",
            url = "https://huggingface.co/mradermacher/Parable-Granite-4.1-3B-Claude-Fable-5-heretic-GGUF/resolve/main/Parable-Granite-4.1-3B-Claude-Fable-5-heretic.Q5_K_M.gguf",
            fileName = "parable_granite_heretic_q5.gguf",
            description = "Parable-Granite 3B с снятой цензурой (abliterated). Q5_K_M ~2.6 ГБ. Отказы упали с 96/100 до 5/100, способности к рассуждениям и коду сохранены. Режим размышлений <think> работает. Для тех случаев, когда обычная Parable отказывается, а ответ нужен. "
        ),

        // ===== 17. NVIDIA NEMOTRON-3-NANO-4B — ОТ NVIDIA, MoE (Q5_K_M) =====
        ModelInfo(
            id = "nemotron_3_nano_4b",
            name = "NVIDIA Nemotron-3-Nano-4B — от NVIDIA, MoE",
            url = BART + "nvidia_Nemotron-3-Nano-4B-GGUF/resolve/main/Nemotron-3-Nano-4B-Q5_K_M.gguf",
            fileName = "nemotron_3_nano_4b_q5.gguf",
            description = "NVIDIA Nemotron-3-Nano-4B — модель от мирового лидера, заточена под edge-устройства (NPC, ассистенты, IoT). Q5_K_M ~3.21 ГБ. Архитектура MoE. Русский язык входит в обучающий корпус (15 языков). Отличная скорость на реальном железе. ВАЖНО: Q5_K_M может не влезть в 3 ГБ — если не грузится, используй Q4_K_M. "
        ),

        // ===== 18. QWEN3.5-4B — 262K КОНТЕКСТ (Q5_K_M) =====
        ModelInfo(
            id = "qwen35_4b",
            name = "Qwen3.5-4B — 262K контекст",
            url = "https://huggingface.co/unsloth/Qwen3.5-4B-GGUF/resolve/main/Qwen3.5-4B-Q5_K_M.gguf",
            fileName = "qwen35_4b_q5.gguf",
            description = "Qwen3.5-4B от Alibaba, Q5_K_M ~3.0 ГБ. Контекст 262144 токена (262K) — огромный. Режим размышлений, function calling, нативное vision (нужен mmproj — не входит). Русский язык — слабый (низкий балл MERA), для английского и мультиязычных задач — отлично. ВАЖНО: Q5_K_M впритык — если не грузится, используй Q4_K_M. "
        ),

                // ===== 19. QVIKHR-3-4B — РУССКАЯ ОТ VIKHR (Q4_K_M) =====
        ModelInfo(
            id = "qvikhr_3_4b",
            name = "QVikhr-3-4B — русская, Qwen3-4B база",
            url = "https://huggingface.co/Vikhrmodels/QVikhr-3-4B-Instruction-GGUF/resolve/main/QVikhr-3-4B-Instruction-Q4_K_M.gguf",
            fileName = "qvikhr_3_4b_q4.gguf",
            description = "Русская модель от команды Vikhr на базе Qwen3-4B. Q4_K_M ~2.5 ГБ. Обучена на датасете GrandMaster2. Ru Arena General = 78.2 — значительно выше базовой Qwen3-4B (64.8). Лучший выбор для русского языка среди моделей этого размера. Официальный GGUF от Vikhrmodels. "
        ),

                // ===== 20. HY-MT2-1.8B — ПЕРЕВОДЧИК, 33 ЯЗЫКА =====
        ModelInfo(
            id = "hy_mt2_1_8b",
            name = "Hy-MT2-1.8B — переводчик, 33 языка",
            url = "https://huggingface.co/tencent/Hy-MT2-1.8B-GGUF/resolve/main/Hy-MT2-1.8B-Q4_K_M.gguf",
            fileName = "hy_mt2_1_8b_q4.gguf",
            description = "Модель-ПЕРЕВОДЧИК от Tencent, а не диалоговая. Q4_K_M, ~1.13 ГБ. 33 языка, включая русский, английский, испанский, немецкий, французский, китайский и другие. Создана специально для телефонов — версия 1.25-bit весит всего 440 МБ. Обгоняет Microsoft Translator и Doubao.\n\n⚠️ ВАЖНО: модель НЕ ведёт диалог — только ПЕРЕВОДИТ. У неё нет промпта по умолчанию.\n\nЧТОБЫ ПОЛЬЗОВАТЬСЯ:\n1. Открой «характер» (🧠) в панели внизу.\n2. Вставь в поле «Инструкция для ИИ» промпт для нужного языка:\n• НА РУССКИЙ: «Переведи следующий текст на русский, без дополнительных объяснений:»\n• НА АНГЛИЙСКИЙ: «Translate the following text into English, without additional explanation:»\n• НА ИСПАНСКИЙ: «Traduce el siguiente texto al español, sin explicaciones adicionales:»\n• НА НЕМЕЦКИЙ: «Übersetze den folgenden Text ins Deutsche, ohne zusätzliche Erklärungen:»\n• НА ФРАНЦУЗСКИЙ: «Traduis le texte suivant en français, sans explication supplémentaire:»\n• НА КИТАЙСКИЙ: «将以下文本翻译成中文，不要额外解释：»\n3. Сохрани и пиши текст на любом языке.\n\nПРИМЕР: промпт «на русский» + пишешь по-испански → получаешь русский перевод. Промпт «на испанский» + пишешь по-русски → получаешь испанский.\n\nЯзык перевода задаётся ПРОМПТОМ, а не языком ввода. "
        ),

        // ===== 21. SMOLVLM-256M — КАРТИНКИ, САМАЯ ЛЁГКАЯ =====
        ModelInfo(
            id = "smolvlm_256m",
            name = "SmolVLM-256M — картинки, самая лёгкая",
            url = "https://huggingface.co/pierretokns/SmolVLM-256M-Instruct-GGUF/resolve/main/SmolVLM-256M-Instruct-Q4_K_M.gguf",
           fileName = "smolvlm_256m_q4.gguf",
           mmprojUrl = "https://huggingface.co/pierretokns/SmolVLM-256M-Instruct-GGUF/resolve/main/mmproj-SmolVLM-256M-Instruct-f16.gguf",
           mmprojFileName = "smolvlm_256m_mmproj.gguf",
            description = "Vision-модель от HuggingFace. 256M, ~300 МБ (модель + проектор). Контекст 8K. Умеет: описание картинок, OCR (распознавание текста), определение координат кнопок. Английский. Идеальна для слабых телефонов. Очень быстрая. "
        ),

        // ===== 22. SMOLVLM2-500M — КАРТИНКИ, КАЧЕСТВЕННЕЕ =====
        ModelInfo(
            id = "smolvlm2_500m",
            name = "SmolVLM2-500M — картинки, качественнее",
            url = "https://huggingface.co/ggml-org/SmolVLM2-500M-Video-Instruct-GGUF/resolve/main/SmolVLM2-500M-Video-Instruct-Q8_0.gguf",
            fileName = "smolvlm2_500m_q4.gguf",
            mmprojUrl = "https://huggingface.co/ggml-org/SmolVLM2-500M-Video-Instruct-GGUF/resolve/main/mmproj-SmolVLM2-500M-Video-Instruct-f16.gguf",
            mmprojFileName = "smolvlm2_500m_mmproj.gguf",
            description = "Vision-модель от HuggingFace. 500M, ~500 МБ (модель + проектор). Контекст 4K. Умеет: описание картинок, визуальный Q&A, OCR, анализ сцен. Английский. Скорость 15–20 токенов/с на телефоне. Качество выше, чем у 256M. "
        )
    )
}
