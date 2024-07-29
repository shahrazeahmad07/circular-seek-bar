package com.example.customcircularseekbar.custom

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.customcircularseekbar.R
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class CircularSeekbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var halfWidth = 0f
    private var halfHeight = 0f

    private var radius = 0f
    private lateinit var rectF: RectF

    private var trackColor: Int = Color.LTGRAY
    private var trackWidth: Float = 10F
    private var progressColor: Int = Color.GRAY
    private var progressWidth: Float = 10f
    private var innerThumbColor: Int = Color.YELLOW
    private var outerThumbColor: Int = Color.BLACK

    private var trackPaint: Paint = Paint()
    private var progressPaint: Paint = Paint()
    private var innerThumbPaint: Paint = Paint()
    private var outerThumbPaint: Paint = Paint()

    private var innerThumbRadius: Float = 10F
    private var outerThumbRadius: Float = 15F
    private var innerPaddingRatio = max(innerThumbRadius, outerThumbRadius)
        set(value) {
            val temp = abs(value)
            field = if (temp <= max(innerThumbRadius, outerThumbRadius)) {
                max(innerThumbRadius, outerThumbRadius)
            } else {
                temp
            }
        }

    var progress = 0f // Range [minProgress, maxProgress]
        set(value) {
            val temp = abs(value)
            field = if (temp >= maxProgress) {
                maxProgress
            } else {
                temp
            }
            circularProgressChangeListener?.onProgressChange(temp)
            invalidate()
        }
    private var minProgress = 0f
    var maxProgress = 100f
        set(value) {
            field = abs(value)
            invalidate()
        }

    private var circularProgressChangeListener: CircularProgressChangeListener? = null

    init {
        if (attrs != null) {
            getAttrs(attrs, defStyleAttr)
        }
        setupPaints()
    }

    private fun getAttrs(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CircularSeekbar,
            defStyleAttr,
            0
        )
        try {
            setTypedArray(typedArray)
        } finally {
            typedArray.recycle()
        }
    }

    fun setOnCircularProgressChangeListener(circularProgressChangeListener: CircularProgressChangeListener) {
        this.circularProgressChangeListener = circularProgressChangeListener
    }

    fun removeProgressChangeListener() {
        this.circularProgressChangeListener = null
    }

    private fun setTypedArray(typedArray: TypedArray) {
        trackColor = typedArray.getColor(R.styleable.CircularSeekbar_circularSeekbarTrackColor, trackColor)
        progressColor = typedArray.getColor(R.styleable.CircularSeekbar_circularSeekbarProgressColor, progressColor)
        innerThumbColor = typedArray.getColor(R.styleable.CircularSeekbar_circularSeekbarInnerThumbColor, innerThumbColor)
        outerThumbColor = typedArray.getColor(R.styleable.CircularSeekbar_circularSeekbarOuterThumbColor, outerThumbColor)

        innerThumbRadius = typedArray.getDimension(R.styleable.CircularSeekbar_circularSeekbarInnerThumbRadius, innerThumbRadius)
        outerThumbRadius = typedArray.getDimension(R.styleable.CircularSeekbar_circularSeekbarOuterThumbRadius, outerThumbRadius)

        innerPaddingRatio = typedArray.getFloat(R.styleable.CircularSeekbar_circularSeekbarInnerPadding, innerPaddingRatio)

        trackWidth = typedArray.getDimension(R.styleable.CircularSeekbar_circularSeekbarTrackWidth, trackWidth)
        progressWidth = typedArray.getDimension(R.styleable.CircularSeekbar_circularSeekbarProgressWidth, progressWidth)

        val maxProgressValue = typedArray.getFloat(R.styleable.CircularSeekbar_circularSeekbarMaxProgress, maxProgress)
        if (this.maxProgress != maxProgressValue) {
            this.maxProgress = maxProgressValue
        }
        val progressValue = typedArray.getFloat(R.styleable.CircularSeekbar_circularSeekbarProgress, progress)
        if (this.progress != progressValue) {
            this.progress = progressValue
        }
    }

    private fun setupPaints() {
        trackPaint.color = trackColor
        trackPaint.style = Paint.Style.STROKE
        trackPaint.strokeWidth = trackWidth
        trackPaint.strokeCap = Paint.Cap.ROUND

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = progressWidth
        progressPaint.strokeCap = Paint.Cap.ROUND

        innerThumbPaint.color = innerThumbColor
        innerThumbPaint.style = Paint.Style.FILL

        outerThumbPaint.color = outerThumbColor
        outerThumbPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        halfWidth = w / 2f
        halfHeight = h / 2f
        radius = halfWidth.coerceAtMost(halfHeight) - innerPaddingRatio

        rectF = RectF(
            halfWidth - radius,
            halfHeight - radius,
            halfWidth + radius,
            halfHeight + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val normalizedProgress = (progress - minProgress) / (maxProgress - minProgress)
        val thumbCenter = normalizedProgress * 360F
        val thumbX = (halfWidth + sin(Math.toRadians(thumbCenter.toDouble())) * radius).toFloat()
        val thumbY = (halfHeight - cos(Math.toRadians(thumbCenter.toDouble())) * radius).toFloat() // Note the negative sign

        canvas.drawArc(rectF, 0F, 360F, false, trackPaint)
        canvas.drawArc(rectF, 270f, thumbCenter, false, progressPaint)
        canvas.drawCircle(thumbX, thumbY, outerThumbRadius, outerThumbPaint)
        canvas.drawCircle(thumbX, thumbY, innerThumbRadius, innerThumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                circularProgressChangeListener?.onProgressChangeStart()
                handleSeekbar(event)
            }

            MotionEvent.ACTION_MOVE -> {
                handleSeekbar(event)
            }

            MotionEvent.ACTION_UP -> {
                circularProgressChangeListener?.onProgressChangeStop(progress)
            }
        }
        return true
    }

    private fun handleSeekbar(event: MotionEvent) {
        val x = event.x - halfWidth
        val y = event.y - halfHeight
        val angle = (Math.toDegrees(atan2(x.toDouble(), -y.toDouble())) + 360) % 360
        val normalizedProgress = angle.toFloat() / 360f
        val temp = minProgress + normalizedProgress * (maxProgress - minProgress)
        if (progress != temp) {
            progress = temp
        }
    }

    interface CircularProgressChangeListener {
        fun onProgressChangeStart()
        fun onProgressChange(progress: Float)
        fun onProgressChangeStop(progress: Float)
    }
}
