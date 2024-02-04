package com.example.micapp

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttachmentsAdapter(
    private val context: Context,
    private val attachments: MutableList<Uri>,
    private val onRemove: (Uri) -> Unit
) : RecyclerView.Adapter<AttachmentsAdapter.AttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attachment, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val uri = attachments[position]
        holder.attachmentName.text = getFileName(uri)
        holder.removeAttachmentButton.setOnClickListener {
            // Remove the item at the current position
            attachments.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, attachments.size)
            onRemove(uri)
        }
    }

    // Placeholder - this method will be needed if we implement option to modify event
    private fun removeAttachment(uri: Uri, position: Int) {
        if (attachments.contains(uri)) {
            attachments.remove(uri)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, attachments.size)
        }
    }

    override fun getItemCount(): Int = attachments.size

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result ?: "Unknown file"
    }

    class AttachmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val attachmentName: TextView = view.findViewById(R.id.attachmentName)
        val removeAttachmentButton: ImageView = view.findViewById(R.id.removeAttachmentButton)
    }
}
