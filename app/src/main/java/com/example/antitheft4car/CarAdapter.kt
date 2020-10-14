package com.example.antitheft4car

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_layout.view.*

class CarAdapter (private val cars : List<Cars>, var clickListener: OnCarItemClickListener) : RecyclerView.Adapter<CarAdapter.CarViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false)

        return  CarViewHolder(inflater)
    }

    override fun getItemCount() = cars.size

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        holder.initialize(cars[position],clickListener)

    }

    class CarViewHolder(val view:View) : RecyclerView.ViewHolder(view){

        var carName = view.carname

        fun initialize(cars: Cars, action:OnCarItemClickListener){
            carName.text = cars.name

            view.setOnClickListener{
                action.onItemClick(cars, adapterPosition)
            }

        }
    }

}

interface OnCarItemClickListener{
    fun onItemClick(cars: Cars, position: Int)
}