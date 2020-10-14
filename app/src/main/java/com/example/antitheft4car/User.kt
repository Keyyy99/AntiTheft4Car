package com.example.antitheft4car

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (val randomCode: String = "-",
                 val lat: String = "-",
                 val lng: String = "-",
                 val location: String = "-",
                 val email: String = "-",
                 val uid: String = "-")