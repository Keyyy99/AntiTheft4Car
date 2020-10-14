package com.example.antitheft4car.ui.video

class Image {

    var path: String?=null
    var name: String?=null

    constructor():this("",""){

    }

    constructor(path: String?, name:String?) {
        this.path = path
        this.name = name
    }

}