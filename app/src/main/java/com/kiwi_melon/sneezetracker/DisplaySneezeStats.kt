package com.kiwi_melon.sneezetracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.InputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DisplaySneezeStats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_sneeze_stats)

        // Hides the status/navigation bar(s)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
                        or android.view.WindowInsets.Type.navigationBars()
            )
        } else { window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN }

        // Defines needed Views.
        val statBtn = findViewById<FloatingActionButton>(R.id.statButton)
        val sneezeWeekText = findViewById<TextView>(R.id.sneezes_month)

        // Gets a list of the sneezes in the current month, does math, and updates sneezeWeekText.
        val monthList = getNumSneezeData("month",
            getCurrentMonth(), getCurrentDay(), getCurrentYear())
        var numSneezesMonth = 0
        if (monthList != null) {
            for (i in monthList) {
                numSneezesMonth += i
            }
        }
        sneezeWeekText.append(" $numSneezesMonth")

        // Changes Activity to MainActivity on click.
        statBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        })
    }

    // Unfinished function.
    fun getAvgSneezeData(type: String, dataFind: String): String {
        val fileString = readFile()

        when (type) {
            "day" -> {}
            "week" -> {}
            "month" -> {}
            "year" -> {}

        }

//        val fileString = readFile()
//        val dateIndex = fileString.indexOf(date)
//        val sneezeIndex = fileString.indexOf(":", dateIndex) + 1
//        val endSneezeIndex = fileString.indexOf(";", sneezeIndex)
//        var x = mutableListOf<String>()
//
//        } else {
//            fileString.subSequence(sneezeIndex, endSneezeIndex).toString()  // This is return
//        }

        fun searchDay(day: String) {
            val dateIndex = fileString.indexOf(day)
            val sneezeIndex = fileString.indexOf(":", dateIndex) + 1
            val endSneezeIndex = fileString.indexOf(";", sneezeIndex)
        }

        return "test"
    }

    // Returns a list of sneeze numbers given type of data requested and date.
    // If type is not recognized, returns null.
    private fun getNumSneezeData(boundaryType: String, month: String, day: String, year: String): MutableList<Int>? {
        val fileString = readFile()
        val fileMap = fileToMap()
        var listOfSneezeStats = mutableListOf<Int>()

        if (boundaryType == "month") {
            for ((key, value) in fileMap) {
                if (key.startsWith(month.toString()) && key.endsWith(year.toString())) {
                    listOfSneezeStats.add(value)
                }
            }
        } else {
            Log.e("getNumSneezeData", "boundaryType is not recognized")
            return null
        }
        return listOfSneezeStats
    }

    fun readFile(): String {
        val file: File = File(filesDir.toString() + "db.txt")
        val inputStream: InputStream = file.inputStream()
        val inpStr = inputStream.bufferedReader().use { it.readText() }
        inputStream.close()
        return inpStr
    }

    fun fileToMap(): MutableMap<String, Int> {
        val file_contents = readFile()
        var date_x = ""
        var sneeze_x = ""
        var listOfDates: MutableList<String> = mutableListOf<String>()
        var listOfSneezes: MutableList<Int> = mutableListOf<Int>()

        var dateLooker = false
        var sneezeLooker = false
        var file_map = mutableMapOf<String, Int>()
        if (file_contents == "") {
            return mutableMapOf<String, Int>()
        } else {
            for (x in file_contents) {
                when (x) {
                    '*' -> {
                        dateLooker = true
                    }
                    ':' -> {
                        dateLooker = false
                        sneezeLooker = true
                        listOfDates.add(date_x)     // Save dates into date_x String
                        date_x = ""                 // Clear date_x String
                    }
                    ';' -> {
                        sneezeLooker = false
                        listOfSneezes.add(sneeze_x.toInt())     // Save sneezes as Int into listOfSneezes
                        sneeze_x = ""                           // Clear sneeze_x str
                    }
                }
                if (dateLooker && x != '*') {
                    date_x += x
                }
                if (sneezeLooker && x != ':') {
                    sneeze_x += x
                }
            }
            for (i in listOfDates.indices) {
                file_map.put(listOfDates[i], listOfSneezes[i])
                // I could also use file_map.remove(key) and file_map.clear()
                // .get(key) returns value  .forEach() performs given action on each item.

            }
            return file_map
        }
    }

    private fun getCurrentMonth(): String {
        val dateFormat: DateFormat = SimpleDateFormat("MM")
        val date = Date()
        return dateFormat.format(date)
    }

    private fun getCurrentDay(): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd")
        val date = Date()
        return dateFormat.format(date)
    }

    private fun getCurrentYear(): String {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy")
        val date = Date()
        return dateFormat.format(date)
    }
}

