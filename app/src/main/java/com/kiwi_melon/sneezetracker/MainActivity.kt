package com.kiwi_melon.sneezetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kiwi_melon.sneezetracker.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    // TODO: Perhaps make it so that it always uses mutablemap, and mutablemap is always changed
    //  using fileToMap() when the file changes. Otherwise, what purpose does mutablemap have?

    // Date Format: *01.18.2020:3;  DO NOT CHANGE!
    // Note for when "db.txt" is empty:
        // ALWAYS use fout variable for FileOutputStream
        // After writing, IMMEDIATELY close fout

    var dateGlobal: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hides the status/navigation bar(s)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
                        or android.view.WindowInsets.Type.navigationBars()
            )
        } else { window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN }

        // Declaring needed Views.
//        val btnView = findViewById<Button>(R.id.btn_view)
        val addBtn = findViewById<Button>(R.id.add_button)
        val minusBtn = findViewById<Button>(R.id.minus_button)
        val statBtn = findViewById<FloatingActionButton>(R.id.statButton)
        val calView = findViewById<CalendarView>(R.id.calendarView)
        val counter_text = findViewById<TextView>(R.id.counter)

        // Below is used for debugging.
//        var fout = FileOutputStream(file)  //This works.
//        fout.write(("*06.15.2021:4;*06.17.2021:2;").toByteArray())
//        fout.close()

        // All Listeners are below.
        calView.setOnDateChangeListener(OnDateChangeListener { view, year, month, dayOfMonth ->
            // Defines and passes the date of the currently selected day on the calendar to
            // a global variable so it can be passed to other functions.
            var monthStr = (month + 1).toString().padStart(2, '0')
            var dayOfMonthStr = dayOfMonth.toString().padStart(2, '0')
            val date: String = "$monthStr.$dayOfMonthStr.$year"
            setGlobalDate(date)

            // Sets the counter text to an appropriate value.
            if (findSneezefromFile(dateGlobal) == "-1") {
                counter_text.text = "0"
            } else {
                counter_text.text = findSneezefromFile(dateGlobal)
            }
        })

        addBtn.setOnClickListener(View.OnClickListener {
            val today = getCurrentDate()
            if (dateGlobal == "0") { // TODO: doesn't this only occur on app start? It is date otherwise.
                addSneeze(today)
            } else {
                addSneeze(dateGlobal)   // Adds a sneeze for the currently selected calendar day.
            } })

        minusBtn.setOnClickListener(View.OnClickListener {
            val today = getCurrentDate()
            if (dateGlobal == "0") {
                removeSneeze(today)
            } else {
                removeSneeze(dateGlobal)
            } })

        statBtn.setOnClickListener(View.OnClickListener {
            // On click, changes activity.
            val intent = Intent(this, DisplaySneezeStats::class.java).apply {}
            startActivity(intent)
        })

        // Button sometimes used for debugging.
//        btnView.setOnClickListener(View.OnClickListener {
//            counter_title.text = readFile()
//        })

        // A try catch to see if there are memory leaks caused by not closing a FileStream
        try {
            Class.forName("dalvik.system.CloseGuard")
                .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
                .invoke(null, true)
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e) } }


    private fun addSneeze(selectedDate: String) {
        Log.i("addSneeze date test", "selectedDate: $selectedDate")
        var dates = fileToMap()
        val counter_text = findViewById<TextView>(R.id.counter)

        /* Since the user can change the selectedDate at any time, current_sneezes must read from
        the file instead of from counter_text. */
        // We also must check for null in case the selectedDate is not in the file.
        var current_sneezes = findSneezefromFile(selectedDate)
        var new_sneezes = 0
        if (current_sneezes == "-1") {
            Log.w("Add Sneeze Warning", "findSneezefromFile: File Not Found, " +
                    "setting currentSneezes to 0, adding date to file.") // This will throw if selectedDate is empty
            current_sneezes = "0"
            new_sneezes = current_sneezes.toInt() + 1
        } else {
            new_sneezes = current_sneezes.toInt() + 1
        }

        dates.remove(selectedDate)
        dates.put(selectedDate, new_sneezes)
        counter_text.text = new_sneezes.toString()

        if (findSneezefromFile(selectedDate) == "-1") {
            Log.e("Add Sneeze Error", "findSneezefromFile Error")
            File(filesDir.toString() + "db.txt").appendText("*$selectedDate:${new_sneezes.toString()};")
            // TODO: Is this line necessary? Won't the code automatically do this whenever fileToMap() is called? Check this!
        } else {
            // Adds 1 the the value for the key of 'selectedDate' to the Mutable Map

        }
        writeToFile(selectedDate, new_sneezes)
        Log.i("addSneeze readFile()", readFile())
        Log.i("addSneeze mutableMap", dates.toString())
    }

    // TODO: Add functionality so that if a date reaches zero sneezes, remove it from the file.
    private fun removeSneeze(selectedDate: String) {
        var dates = fileToMap()
        val counter_text = findViewById<TextView>(R.id.counter)

        var new_sneezes = 0
        var current_sneezes = (counter_text.text as String).toInt()   // If code no longer gets correct number, read from file instead and add check for null
        if (current_sneezes > 0) {
            new_sneezes = current_sneezes - 1
        } else {
            new_sneezes = current_sneezes
            val toast = Toast.makeText(applicationContext, "Cannot have less than 0 sneezes.", Toast.LENGTH_SHORT)
            toast.show()
        }

        dates.remove(selectedDate)
        dates.put(selectedDate, new_sneezes)
        counter_text.text = new_sneezes.toString()

        if (findSneezefromMap(dates, selectedDate) == null) {
            val toast = Toast.makeText(applicationContext, "Cannot have less than 0 sneezes.", Toast.LENGTH_SHORT)
            Log.i("Remove If null", readFile())
        } else {
            writeToFile(selectedDate, new_sneezes)
            Log.i("Remove If readFile", readFile())
        }

        Log.i("removeSneeze readFile()", readFile())
        Log.i("removeSneeze mutableMap", dates.toString())
    }

    // Reads the file text and returns it as a string.
    fun readFile(): String {
        val file: File = File(filesDir.toString() + "db.txt")
        val inputStream: InputStream = file.inputStream()
        val inpStr = inputStream.bufferedReader().use { it.readText() }
        inputStream.close()
        return inpStr
    }

    // Converts the data file to a MutableMap.
    private fun fileToMap(): MutableMap<String, Int> {
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

    // Returns either the number of sneezes for a given day or null if date or number does not exist.
    private fun findSneezefromMap(fileMap: MutableMap<String, Int>, date: String): Int? {
        return fileMap.get(date)
    }

    // Returns number of sneezes for a given day as a String.
    private fun findSneezefromFile(date: String): String {
        val fileString = readFile()
        val dateIndex = fileString.indexOf(date)
        val sneezeIndex = fileString.indexOf(":", dateIndex) + 1
        val endSneezeIndex = fileString.indexOf(";", sneezeIndex)
        var x = mutableListOf<String>()
        when {
            dateIndex == -1 -> { x.add("dateIndex") }
            sneezeIndex == -1 -> { x.add("sneezeIndex") }
            endSneezeIndex == -1 -> { x.add("endSneezeIndex") }
        }
        return if (sneezeIndex == -1 || endSneezeIndex == -1 || dateIndex == -1) {
            Log.w("UnofficialIndexError", x.toString())
            Log.w("UnofficialIndexError", "dateIndex: $dateIndex, " +
                    "sneezeIndex: $sneezeIndex, " +
                    "endSneezeIndex: $endSneezeIndex")
            "-1"
        } else {
            fileString.subSequence(sneezeIndex, endSneezeIndex).toString()  // This is return
        }
    }

    // Given a day, will overwrite the number of sneezes the date currently contains.
    private fun writeToFile(day: String, writeNumSneezes: Int) {
        var fileString = ""
        fileString = readFile()
        val file = File(filesDir.toString() + "db.txt")
        val dayIndex = fileString.indexOf(day)
        val sneezeIndex = fileString.indexOf(":", dayIndex) + 1
        val endSneezeIndex = fileString.indexOf(";", sneezeIndex)
        var numSneezes = fileString.subSequence(sneezeIndex, endSneezeIndex)
        fileString = fileString.replaceRange(sneezeIndex, endSneezeIndex, writeNumSneezes.toString() )

        var fout = FileOutputStream(file)
        fout.write((fileString).toByteArray())
        fout.close()
    }

    // Returns the current date in a specific format.
    fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("MM.dd.yyyy")
        return dateFormat.format(date)
    }

    // Sets a global variable to be passed between functions.
    fun setGlobalDate(date: String) {
        dateGlobal = date
    }

}


