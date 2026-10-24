package com.example.tgz3dpanel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val overlayRequest = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }
        root.addView(TextView(this).apply {
            text = "TGZ3D Panel"
            textSize = 28f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(TextView(this).apply {
            text = "Плавающее окно поверх игры\n\nКнопка дерева пока демонстрационная."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        })
        root.addView(Button(this).apply {
            text = "Открыть плавающее окно"
            setOnClickListener { requestOverlay() }
        })
        setContentView(root)
    }

    private fun requestOverlay() {
        if (Settings.canDrawOverlays(this)) {
            startOverlay()
        } else {
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                overlayRequest
            )
        }
    }

    private fun startOverlay() {
        startService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Плавающая панель запущена", Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Uses activity result callback for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == overlayRequest) {
            if (Settings.canDrawOverlays(this)) startOverlay()
            else Toast.makeText(this, "Разрешение не выдано", Toast.LENGTH_LONG).show()
        }
    }
}
