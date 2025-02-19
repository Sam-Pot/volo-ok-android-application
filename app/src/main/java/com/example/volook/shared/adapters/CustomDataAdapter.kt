package com.example.volook.shared.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.volook.R

class CustomDataAdapter<K,V>(context: Context, resource: Int, objects: List<CustomCardData<K,V>>):
    ArrayAdapter<CustomCardData<K,V>>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_layout, parent, false)
            val element = getItem(position) as CustomCardData<K,V>

            val textViewTitle = view.findViewById<TextView>(R.id.card_title)
            val textViewBody = view.findViewById<TextView>(R.id.card_description)

            var bodyText = ""

            if(element!=null){
                if(element.body?.keys != null){
                    for(key in element.body.keys){
                        bodyText+= (element.body[key].toString()+"\n")
                    }
                }
                textViewTitle.text = element.title
                textViewBody.text = bodyText
            }
            return view
        }
    }
