package com.tgz3d.panel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "TGZ3D Panel"
            textSize = 26f
        }

        val startButton = Button(this).apply {
            text = "Открыть панель"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                    Toast.makeText(
                        this@MainActivity,
                        "Разреши отображение поверх других приложений",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    startService(
                        Intent(this@MainActivity, OverlayService::class.java)
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Панель запущена",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }

        layout.addView(title)
        layout.addView(startButton)
        setContentView(layout)
    }
}
