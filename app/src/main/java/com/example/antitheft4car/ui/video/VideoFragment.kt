package com.example.antitheft4car.ui.video

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.antitheft4car.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.android.synthetic.main.image_item_list.*
import kotlinx.coroutines.delay
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class VideoFragment : Fragment() {

    private var m2Database: FirebaseDatabase? = null
    private var mDatabase: FirebaseDatabase? = null
    private var storageRef2: StorageReference? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var m2DatabaseReference: DatabaseReference? = null
    private val secondStorage = Firebase.storage("gs://bait2123-202003-01.appspot.com")
    private val storage = Firebase.storage
    private lateinit var user: FirebaseUser
    private var status: String = ""
    private var day: String = ""
    private var hour: String = ""
    private var min: String = ""
    private var sec: String = ""
    private var preSec: String = ""
    private var preMin: String = ""
    private var component: String = "camera"
    private lateinit var dpd: DatePickerDialog
    private lateinit var selectYear: String
    private lateinit var selectMonth: String
    private lateinit var selectDay: String
    private lateinit var selectHour: String
    private lateinit var selectMinute: String
    lateinit var mRecyclerView: RecyclerView
    lateinit var ref: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (context as AppCompatActivity).supportActionBar!!.title = "Surveillance"

        val rootView = inflater.inflate(R.layout.fragment_video, container, false)

        mRecyclerView = rootView.findViewById(R.id.image_list) as RecyclerView

        mRecyclerView.layoutManager = LinearLayoutManager(context)

        mRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val secondary = FirebaseApp.getInstance("secondary")

        user = FirebaseAuth.getInstance().currentUser!!

        mDatabase = FirebaseDatabase.getInstance()
        m2Database = FirebaseDatabase.getInstance(secondary)

        mDatabaseReference = mDatabase!!.reference.child("Camera_Image")
        m2DatabaseReference = m2Database!!.reference.child("PI_01_A_CONTROL")

        m2DatabaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                status = snapshot.child(component).value.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val c: Calendar = Calendar.getInstance()
        val currentDay = c.get(Calendar.DAY_OF_MONTH)
        val currentMonth = c.get(Calendar.MONTH)
        val currentYear = c.get(Calendar.YEAR)

        txtDate.setOnClickListener() {
            dpd = DatePickerDialog(
                context!!,
                DatePickerDialog.OnDateSetListener { view, year, month, day ->
                    date.setText(
                        day.toString() +
                                "/" + (month + 1).toString() + "/" + year.toString()
                    )

                    selectDay = day.toString()

                    var sMonth = ""
                    if ((month + 1) < 10) {
                        sMonth = "0" + (month + 1).toString()
                        selectMonth = sMonth
                    } else {
                        sMonth = (month + 1).toString()
                        selectMonth = sMonth
                    }
                    selectYear = year.toString()
                }
                ,
                currentYear,
                currentMonth,
                currentDay)
            dpd.show()
        }

        timePicker.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                timeView.text = SimpleDateFormat("HH:mm").format(c.time)
                selectHour = SimpleDateFormat("HH").format(c.time)
                selectMinute = SimpleDateFormat("mm").format(c.time)

            }
            TimePickerDialog(
                context,
                timeSetListener,
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSearch.setOnClickListener() {
            mRecyclerView.visibility = View.VISIBLE
            image_id.text = ""
            image_show.setImageAlpha(0)
            no_image.visibility = View.INVISIBLE
            history()
        }

        btnOn.setOnClickListener() {
            no_image.visibility = View.INVISIBLE
            image_show.setImageAlpha(255)
            changeStatus()
        }

    }

    private fun changeStatus() {
        try {
            val mainHandler = Handler()

            if (status == "0") {
                status = "1"
                Toast.makeText(context, "Connecting", Toast.LENGTH_SHORT).show()
            } else {
                status = "0"
                Toast.makeText(context, "Stop Capturing", Toast.LENGTH_SHORT).show()
            }

            m2DatabaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    m2DatabaseReference!!.child(component).setValue(status)
                }
            })

            if (status == "1") {
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
                        if (sec == "10" || sec == "20" || sec == "30" || sec == "40" || sec == "50" || sec == "00") {
                            loadImage(min, sec)
                        }

                        mainHandler.postDelayed(this, 1000)

                    }


                })
            }

        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
        }
    }

    fun loadImage(minR: String, sec: String) {

        if (sec != "10" && sec != "00") {
            preSec = (sec.toInt() - 10).toString()
            preMin = minR
        } else if (sec == "10") {
            preSec = "00"
            preMin = minR
        } else {
            preMin = (minR.toInt() - 1).toString()
            preSec = "50"
        }

        storageRef2 =
            secondStorage.getReferenceFromUrl("gs://bait2123-202003-01.appspot.com/PI_01_A_CONTROL/cam_" + day + hour + preMin + preSec + ".jpg")

        val storageRef = storage.reference
        val upRef = storageRef.child("Camera_Image/cam_" + day + hour + preMin + preSec + ".jpg")
        val localFile = File.createTempFile("images", "jpg")

        storageRef2!!.getFile(localFile).addOnSuccessListener {
            var file = Uri.fromFile(File(localFile.toURI()))
            upRef.putFile(file)
            progress.setVisibility(View.VISIBLE)
            mDatabaseReference!!.addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var filename =
                        "gs://antitheft4car.appspot.com/Camera_Image/cam_"
                    mDatabaseReference!!.child(day).child(hour)
                        .child(preMin).child(preSec).child("path")
                        .setValue(filename + day + hour + preMin + preSec + ".jpg")
                    mDatabaseReference!!.child(day).child(hour)
                        .child(preMin).child(preSec).child("name")
                        .setValue("cam_" + day + hour + preMin + preSec)

                }
            })

            activity?.runOnUiThread {
                mRecyclerView.visibility = View.INVISIBLE
                GlideApp.with(context as AppCompatActivity)
                    .load(storageRef2)
                    .override(550, 200)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progress.setVisibility(View.GONE)
                            return false;
                        }
                    })
                    .into(image_show)

                image_id.text = "cam_" + day + hour + preMin + preSec

            }

        }

    }

    fun history() {

        if (date.text.toString().trim().isEmpty()) {
            date.error = "Please choose a day."
            date.requestFocus()
            return
        } else {
            date.setError(null)
        }

        if (timeView.text.toString().trim().isEmpty()) {
            timeView.error = "Please choose a time"
            timeView.requestFocus()
            return
        } else {
            timeView.setError(null)
        }

        ref = FirebaseDatabase.getInstance().getReference("Camera_Image")
        var query =
            ref.child(selectYear + selectMonth + selectDay).child(selectHour).child(selectMinute)

        val option = FirebaseRecyclerOptions.Builder<Image>()
            .setQuery(query, Image::class.java)
            .build()

        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<Image, ImageViewHolder>(option) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ImageViewHolder {
                    val itemView = LayoutInflater.from(context)
                        .inflate(R.layout.image_item_list, parent, false)
                    return ImageViewHolder(itemView)
                }

                override fun onBindViewHolder(
                    holder: ImageViewHolder,
                    position: Int,
                    model: Image
                ) {
                    var imageid: String = getRef(position).key.toString()

                    ref.child(imageid).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            holder.mtitle.setText(model.name)
                            GlideApp.with(holder.itemView.context)
                                .load(storage.getReferenceFromUrl(model.path!!))
                                .into(holder.mimage)
                        }

                    })
                }

                override fun getItemCount(): Int {
                    return super.getItemCount()
                }
            }

        mRecyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
        firebaseRecyclerAdapter.notifyDataSetChanged()
        if (firebaseRecyclerAdapter.itemCount == 0) {
            val subHandler = Handler()
            subHandler.postDelayed(object :Runnable{
                override fun run() {
                    no_image.visibility = View.VISIBLE
                }

            },1000)
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mtitle: TextView = itemView!!.findViewById<TextView>(R.id.desc)
        var mimage: ImageView = itemView!!.findViewById<ImageView>(R.id.camera_image)
    }
}
