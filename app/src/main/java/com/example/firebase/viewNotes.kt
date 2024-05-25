package com.example.firebase

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class viewNotes : AppCompatActivity() {

    private var noteId: String? = null
    private lateinit var notedb: DatabaseReference
    private lateinit var viewTitle: TextView
    private lateinit var viewDesc: TextView
    private lateinit var updateNote: Button
    private lateinit var linearUpdate: LinearLayout
    private lateinit var upTitle: EditText
    private lateinit var upDesc: EditText
    private lateinit var btnUpdateNote: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_notes)
        val bundle: Bundle? = intent.extras

        viewTitle = findViewById(R.id.vTittle)
        viewDesc = findViewById(R.id.vDesc)
        updateNote = findViewById(R.id.btn_update)
        val keluar: Button = findViewById(R.id.btn_ext)
        linearUpdate = findViewById(R.id.lin_upt)
        upTitle = findViewById(R.id.upt_tittle)
        upDesc = findViewById(R.id.upt_desc)
        btnUpdateNote = findViewById(R.id.btUp)

        val title = bundle!!.getString("judul")
        val desc = bundle.getString("desk")
        noteId = bundle.getString("id")

        viewTitle.text = title
        viewDesc.text = desc

        notedb = FirebaseDatabase.getInstance("https://pam9-d569e-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("notes").child(noteId!!)

        updateNote.setOnClickListener {
            linearUpdate.visibility = View.VISIBLE
            viewTitle.visibility = View.GONE
            viewDesc.visibility = View.GONE
            upTitle.setText(viewTitle.text)
            upDesc.setText(viewDesc.text)
        }

        btnUpdateNote.setOnClickListener {
            updateNote()
        }

        keluar.setOnClickListener {
            finish()
        }
    }

    private fun updateNote() {
        val updatedTitle = upTitle.text.toString()
        val updatedDesc = upDesc.text.toString()

        if (updatedTitle.isEmpty() || updatedDesc.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val note = mapOf(
                    "judul" to updatedTitle,
                    "deskripsi" to updatedDesc
                )
                notedb.updateChildren(note).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@viewNotes, "Note updated successfully", Toast.LENGTH_SHORT).show()
                    linearUpdate.visibility = View.GONE
                    viewTitle.visibility = View.VISIBLE
                    viewDesc.visibility = View.VISIBLE
                    viewTitle.text = updatedTitle
                    viewDesc.text = updatedDesc
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@viewNotes, "Failed to update note", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
