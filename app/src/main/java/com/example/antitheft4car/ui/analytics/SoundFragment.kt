package com.example.antitheft4car.ui.analytics

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.antitheft4car.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SoundFragment : Fragment(), OnChartValueSelectedListener {

    private var m2Database: FirebaseDatabase? = null
    private var valueDatabaseReference: DatabaseReference? = null
    private var day: String = ""
    private var hour: String = ""
    private var min: String = ""
    private var sec: String = ""
    private var previous: String = ""
    private var soundValue: String = ""
    private lateinit var chart: LineChart
    private val xLabel = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (context as AppCompatActivity).supportActionBar!!.title = "Sound Analytics"

        val rootView = inflater.inflate(R.layout.fragment_sound, container, false)

        chart = rootView.findViewById(R.id.chart1) as LineChart

        chart.setOnChartValueSelectedListener(this)

        chart.description.isEnabled
        chart.setTouchEnabled(true)
        chart.isDragEnabled
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(true)
        chart.setBackgroundColor(Color.TRANSPARENT)
        var data : LineData = LineData()
        data.setValueTextColor(Color.BLACK)
        data.setHighlightEnabled(true)
        chart.setData(data)

        var l:Legend = chart.legend
        l.setForm(Legend.LegendForm.LINE)
        l.setTextColor(Color.BLACK)

        var leftAxis = chart.getAxisLeft()
        leftAxis.setTextColor(Color.BLACK)
        leftAxis.setAxisMaximum(1000f)
        leftAxis.setAxisMinimum(0f)
        leftAxis.setDrawGridLines(true)

        var rightAxis = chart.getAxisRight()
        rightAxis.setEnabled(false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val secondary = FirebaseApp.getInstance("secondary")

        m2Database = FirebaseDatabase.getInstance(secondary)

        val mainHandler = Handler()
        mainHandler.post(object : Runnable {
            override fun run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val current = LocalDateTime.now()
                    var upper = DateTimeFormatter.ofPattern("yyyy" + "MM" + "dd")
                    day = current.format(upper)
                    var middle = DateTimeFormatter.ofPattern("HH")
                    hour = current.format(middle)
                    var below = DateTimeFormatter.ofPattern("mm")
                    min = current.format(below)
                    var below2 = DateTimeFormatter.ofPattern("ss")
                    sec = current.format(below2)
                } else {
                    var date = Date()
                    val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
                    val answer: String = formatter.format(date)
                    Log.d("answer", answer)
                }

                valueDatabaseReference =
                    m2Database!!.reference.child("PI_01_A_$day").child(hour).child(min + sec)

                valueDatabaseReference?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        soundValue = snapshot.child("sound").value.toString()

                        if (previous != snapshot.key) {
                            if (soundValue != "null") {
                                xLabel.add(snapshot.key.toString())
                                addEntry(soundValue)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
                mainHandler.postDelayed(this, 1000)
            }
        })

    }

    fun addEntry(soundValue:String){
        var data=chart.data
        if(data!=null){
            var set = data.getDataSetByIndex(0)

            if(set==null){
                set = createSet()
                data.addDataSet(set)
            }

            var value = soundValue.toFloat()

            data.addEntry(Entry(set.entryCount.toFloat(), value),0)
            data.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.setVisibleXRangeMaximum(1000F)
            chart.moveViewToX(data.entryCount.toFloat())
            chart.invalidate()
        }
    }

    fun createSet():LineDataSet {
        var xl = chart.getXAxis()
        xl.setTextColor(Color.BLACK)
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)
        xl.setEnabled(true)
        xl.setValueFormatter(object:ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                return xLabel[value.toInt()]
            }
        })

        var set = LineDataSet(null, "Dynamic Data")
        set.setAxisDependency(YAxis.AxisDependency.LEFT)
        set.setColor(ColorTemplate.getHoloBlue())
        set.setCircleColor(Color.RED)
        set.setLineWidth(2f)
        set.setCircleRadius(4f)
        set.setFillAlpha(65)
        set.setFillColor(ColorTemplate.getHoloBlue())
        set.setHighLightColor(Color.rgb(244, 117, 117))
        set.setValueTextColor(Color.RED)
        set.setValueTextSize(9f)
        set.setDrawValues(false)
        return set
    }

    override fun onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i("Entry selected", e.toString());    }
}