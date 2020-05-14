package com.soonsim.certviewer

import android.app.Activity
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class CertDataAdapter(private val mActivity: Activity, private val mCertData: List<CertData>) : RecyclerView.Adapter<CertDataAdapter.ViewHolder>() {
    private var mOnItemClickListener: View.OnClickListener?=null
    private var mOnItemLongClickListener: View.OnLongClickListener? = null

    // The advantage of inner class over nested class is that,
    // it is able to access members of outer class even it is private.
    // Inner class keeps a reference to an object of outer class.
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewDetail: TextView = itemView.findViewById(R.id.textViewDetail)

        init {
            itemView.tag=this
            itemView.setOnClickListener(mOnItemClickListener)
            itemView.setOnLongClickListener(mOnItemLongClickListener)
        }
    }

    fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        mOnItemClickListener = itemClickListener
    }

    fun setOnItemLongClickListener(itemClickListener: View.OnLongClickListener) {
        mOnItemLongClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mCertData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val certdata=mCertData[position]

        // image
        var rid=R.drawable.error
        var colid=R.color.colorError
        if (mCertData[position].isvalid) {
            rid = R.drawable.verified_user
            colid = R.color.colorPrimary
        }
        Glide.with(mActivity).load(rid).into(holder.imageView)
        holder.imageView.setColorFilter(mActivity.resources.getColor(colid), PorterDuff.Mode.SRC_IN);

        // title
        holder.textViewTitle.text=certdata.cn
        // detail
        holder.textViewDetail.text=certdata.getSummary()
    }

    public override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (holder.adapterPosition == RecyclerView.NO_POSITION)
            return
    }
}