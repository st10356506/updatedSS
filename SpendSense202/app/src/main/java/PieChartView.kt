package com.example.spendsense.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class PieChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 35f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
    }

    private val rectF = RectF()

    private val expenses = mapOf(
        "Rent" to 5000f,
        "Groceries" to 2000f,
        "Transport" to 1000f,
        "Entertainment" to 1500f,
        "Utilities" to 800f
    )

    private val colors = listOf(
        Color.parseColor("#A7D1AB"),
        Color.parseColor("#8FBC8F"),
        Color.parseColor("#66CDAA"),
        Color.parseColor("#458B74"),
        Color.parseColor("#2E8B57")
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val total = expenses.values.sum()
        if (total == 0f) return

        val padding = 40f
        val blockMargin = 20f
        val blockHeight = height.toFloat() - blockMargin * 2

        // Background block
        val blockRect = RectF(blockMargin, blockMargin, width - blockMargin, blockHeight)
        canvas.drawRoundRect(blockRect, 50f, 50f, backgroundPaint)
        canvas.drawRoundRect(blockRect, 50f, 50f, borderPaint)

        // Pie Chart
        val chartSize = Math.min(width, height) * 0.5f
        val left = (width - chartSize) / 2
        val top = blockMargin + 40f
        val right = left + chartSize
        val bottom = top + chartSize
        rectF.set(left, top, right, bottom)

        var startAngle = 0f
        var colorIndex = 0

        for ((_, amount) in expenses) {
            val sweepAngle = (amount / total) * 360f
            paint.color = colors[colorIndex % colors.size]
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)

            // Percentage label
            val labelAngle = startAngle + sweepAngle / 2
            val labelRadius = chartSize / 2.5f
            val angleRad = Math.toRadians(labelAngle.toDouble())
            val labelX = (width / 2) + (cos(angleRad) * labelRadius)
            val labelY = (top + chartSize / 2) + (sin(angleRad) * labelRadius)

            paint.color = Color.BLACK
            paint.textSize = 35f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${(amount / total * 100).toInt()}%", labelX.toFloat(), labelY.toFloat(), paint)

            startAngle += sweepAngle
            colorIndex++
        }

        // Legend
        drawLegend(canvas, 75f, bottom + 30f)
    }

    private fun drawLegend(canvas: Canvas, startX: Float, startY: Float) {
        var y = startY
        val iconSize = 20f
        val textPadding = 10f

        expenses.keys.forEachIndexed { index, category ->
            textPaint.color = colors[index % colors.size]
            canvas.drawRect(startX, y, startX + iconSize, y + iconSize, textPaint)

            textPaint.color = Color.BLACK
            canvas.drawText(category, startX + iconSize + textPadding, y + iconSize, textPaint)

            y += 50f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 700
        val desiredHeight = 700
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}
