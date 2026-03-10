package ci.nsu.moble.main

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // Карта с разрешёнными цветами (название в нижнем регистре -> код цвета)
    private val colorMap = mapOf(
        "red" to Color.parseColor("#ff0000"),
        "orange" to Color.parseColor("#ff7700"),
        "yellow" to Color.parseColor("#FFFF00"),
        "green" to Color.parseColor("#35e625"),
        "blue" to Color.parseColor("#0335ff"),
        "indigo" to Color.parseColor("#3e1d7d"),
        "violet" to Color.parseColor("#8340ff")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText = findViewById<EditText>(R.id.editText)
        val button = findViewById<Button>(R.id.button)

        button.setOnClickListener {
            val colorText = editText.text.toString().trim().lowercase()
            if (colorText.isNotEmpty()) {
                if (colorMap.containsKey(colorText)) {
                    val color = colorMap[colorText]
                    color?.let {
                        button.setBackgroundColor(it)
                    }
                } else {
                    Toast.makeText(this, "Этого цвета нет в списке, попробуйте ещё раз", Toast.LENGTH_LONG).show()
                    Log.v("colorProblem","Этого цвета нет в списке, попробуйте ещё раз")
                }
            } else {
                Toast.makeText(this, "Введите цвет", Toast.LENGTH_SHORT).show()
                Log.v("colorProblem","Пустое поле")
            }
        }
    }
}
