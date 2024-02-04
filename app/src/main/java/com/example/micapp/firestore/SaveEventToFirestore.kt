package com.example.micapp.firestore

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SaveEventToFirestore {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun saveEventWithAttachments(
        event: Event,
        attachmentUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Save the event information to Firestore
        db.collection("Events")
            .add(event)
            .addOnSuccessListener { documentReference ->
                Log.d("SaveEventToFirestore", "DocumentSnapshot written with ID: ${documentReference.id}")

                // Upload attachments URI's if any
                if (attachmentUris.isNotEmpty()) {
                    uploadAttachments(documentReference.id, attachmentUris,
                        { urls ->
                            onSuccess(urls)
                        },
                        onFailure)
                } else {
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.w("SaveEventToFirestore", "Error adding document", e)
                onFailure(e)
            }
    }


    private fun uploadAttachments(eventId: String, uris: List<Uri>, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val uploadTasks = uris.map { uri ->
            val ref = storage.reference.child("event_attachments/$eventId/${uri.lastPathSegment}")
            ref.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                ref.downloadUrl
            }
        }

        // List of reveived URI's
        Tasks.whenAllSuccess<Uri>(uploadTasks)
            .addOnSuccessListener { downloadUris ->
                val downloadUrls = downloadUris.map { it.toString() }

                db.collection("Events").document(eventId).update("attachments", downloadUrls)
                    .addOnSuccessListener {
                        onSuccess(downloadUrls)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
