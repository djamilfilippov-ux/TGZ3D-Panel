package com.example.tgz3dpanel

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var body: LinearLayout? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var collapsed = false

    private val panelColor = Color.rgb(28, 30, 43)
    private val accentColor = Color.rgb(90, 112, 245)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (panel == null) showPanel()
        return START_NOT_STICKY
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun makeLabel(textValue: String, size: Float) = TextView(this).apply {
        text = textValue
        textSize = size
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun showPanel() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(panelColor, 14)
            setPadding(dp(10), dp(8), dp(10), dp(10))
            elevation = dp(8).toFloat()
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = makeLabel("TGZ3D Panel", 16f).apply {
            typeface = Typeface.DEFAULT_BOLD
            contentDescription = "Перетащить панель"
            setOnTouchListener(DragListener())
        }
        val collapse = Button(this).apply {
            text = "−"
            textSize = 18f
            minWidth = dp(40)
            setPadding(0, 0, 0, 0)
            contentDescription = "Свернуть или развернуть"
            setOnClickListener { toggleCollapse() }
        }
        val close = Button(this).apply {
            text = "×"
            textSize = 18f
            minWidth = dp(40)
            setPadding(0, 0, 0, 0)
            contentDescription = "Закрыть панель"
            setOnClickListener { stopSelf() }
        }
        header.addView(title, LinearLayout.LayoutParams(0, dp(44), 1f))
        header.addView(collapse)
        header.addView(close)
        root.addView(header)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, 0)
        }
        val treeButton = Button(this).apply {
            text = "🌲 9999 дерева"
            isAllCaps = false
            textSize = 15f
            setTextColor(Color.WHITE)
            background = rounded(accentColor, 10)
            setOnClickListener {
                Toast.makeText(
                    this@OverlayService,
                    "Демонстрация: функция 9999 дерева не подключена к игре",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        content.addView(treeButton, LinearLayout.LayoutParams(dp(210), dp(48)))
        content.addView(makeLabel("Перемещай за заголовок", 11f).apply {
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        })
        root.addView(content)

        body = content
        panel = root
        windowParams = WindowManager.LayoutParams(
            dp(250),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(120)
        }
        wm.addView(root, windowParams)
    }

    private fun toggleCollapse() {
        val content = body ?: return
        collapsed = !collapsed
        content.visibility = if (collapsed) View.GONE else View.VISIBLE
    }

    private inner class DragListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var touchX = 0f
        private var touchY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val p = windowParams ?: return false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = p.x
                    initialY = p.y
                    touchX = event.rawX
                    touchY = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    p.x = initialX + (event.rawX - touchX).toInt()
                    p.y = initialY + (event.rawY - touchY).toInt()
                    try {
                        panel?.let { wm.updateViewLayout(it, p) }
                    } catch (_: RuntimeException) {
                        stopSelf()
                    }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return true
            }
            return false
        }
    }

    override fun onDestroy() {
        panel?.let { view ->
            try { wm.removeView(view) } catch (_: Exception) { }
        }
        panel = null
        body = null
        super.onDestroy()
    }
}
