package org.nehuatl.sample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class MatrixChatBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val fontSize = 30f // Размер символов
    private val lineHeight = fontSize * 1.05f
    private val speed = 2.5f // Скорость падения
    private val maxLines = 15 // Количество одновременно видимых строк
    private val maxPoolSize = 30
    private val words = arrayOf("Нео", "Батя", "Меч Правды", "Ковчег", "Иди за белым кроликом")

    private val paint = Paint().apply {
        color = Color.parseColor("#21A038")
        textSize = fontSize
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
        alpha = 40 // Очень блеклый (чем меньше число, тем прозрачнее)
    }

    private var columns = 0
    private val linePool = arrayOfNulls<String>(maxPoolSize)
    private val linePoolIndex = IntArray(maxLines) { -1 }
    private val lineY = FloatArray(maxLines)
    private val printed = IntArray(maxLines)
    private var nextPoolSlot = 0
    private var screenH = 0f
    private var frame = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenH = h.toFloat()
        columns = (w / fontSize).toInt() + 1
        
        // Инициализируем пул строк
        for (i in 0 until maxPoolSize) {
            linePool[i] = generateLine()
        }
        
        // Расставляем строки по экрану
        for (i in 0 until maxLines) {
            linePoolIndex[i] = i % maxPoolSize
            lineY[i] = i * lineHeight * 1.5f
            printed[i] = 0
        }
        nextPoolSlot = maxLines % maxPoolSize
    }

    private fun generateLine(): String {
        return if (Random.nextFloat() < 0.12f) {
            words[Random.nextInt(words.size)]
        } else {
            CharArray(columns) { if (Random.nextFloat() > 0.5f) '0' else '1' }.joinToString("")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE) // Белый фон, как у вашего чата
        frame++

        // Обновляем состояние строк (печать и движение)
        for (i in 0 until maxLines) {
            val poolIdx = linePoolIndex[i]
            if (poolIdx < 0) continue
            val line = linePool[poolIdx] ?: continue
            
            // Эффект печатной машинки: постепенно открываем строку
            if (frame % 3 == 0 && printed[i] < line.length) {
                printed[i] += 2
            }
            lineY[i] -= speed
        }

        // Если первая строка ушла вверх, сдвигаем все и добавляем новую
        if (lineY[0] < -lineHeight) {
            for (i in 0 until maxLines - 1) {
                linePoolIndex[i] = linePoolIndex[i + 1]
                lineY[i] = lineY[i + 1]
                printed[i] = printed[i + 1]
            }
            linePool[nextPoolSlot] = generateLine()
            linePoolIndex[maxLines - 1] = nextPoolSlot
            lineY[maxLines - 1] = lineY[maxLines - 2] + lineHeight
            printed[maxLines - 1] = 0
            nextPoolSlot = (nextPoolSlot + 1) % maxPoolSize
        }

        // Отрисовываем строки
        for (i in 0 until maxLines) {
            val poolIdx = linePoolIndex[i]
            if (poolIdx < 0) continue
            val line = linePool[poolIdx] ?: continue
            val y = lineY[i]
            
            // Пропускаем строки за пределами экрана
            if (y > screenH + lineHeight || y < -lineHeight) continue
            
            val limit = printed[i].coerceAtMost(line.length)
            for (c in 0 until limit) {
                canvas.drawText(line[c].toString(), c * fontSize, y, paint)
            }
        }

        // Обновляем анимацию (100 мс — ~10 FPS)
        postInvalidateDelayed(80)
    }
}
