package com.example.mobile_service_app

import android.content.Context import android.view.LayoutInflater import android.view.View import android.view.ViewGroup import android.widget.BaseAdapter import android.widget.TextView import com.example.mobile_service_app.ServiceModel

class ServiceAdapter( private val context: Context, private val services: List<ServiceModel> ) : BaseAdapter() {

    override fun getCount(): Int = services.size

    override fun getItem(position: Int): Any = services[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View { val view = convertView ?: LayoutInflater.from(context) .inflate(R.layout.item_service, parent, false)

        val service = services[position]

        view.findViewById<TextView>(R.id.tvServiceName).text = service.serviceName
        view.findViewById<TextView>(R.id.tvDescription).text = service.description
        view.findViewById<TextView>(R.id.tvPrice).text = "₹${service.price}"
        view.findViewById<TextView>(R.id.tvTime).text = service.estimatedTime

        return view }



}