package com.example.firebase

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class InsertNoteActivity : AppCompatActivity(), View.OnClickListener {

    private var tvEmail: TextView? = null
    private var tvUid: TextView? = null
    private lateinit var btnKeluar: Button
    private var mAuth: FirebaseAuth? = null
    private var etTitle: EditText? = null
    private var etDesc: EditText? = null
    private lateinit var btnSubmit: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var notedb: DatabaseReference
    private var noteList: MutableList<Note> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_note)

        tvEmail = findViewById(R.id.tv_email)
        tvUid = findViewById(R.id.tv_uid)
        btnKeluar = findViewById(R.id.btn_keluar)
        mAuth = FirebaseAuth.getInstance()
        btnKeluar.setOnClickListener(this)
        etTitle = findViewById(R.id.et_title)
        etDesc = findViewById(R.id.et_description)
        btnSubmit = findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener(this)

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(this, noteList)
        recyclerView.adapter = noteAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser!!.uid
        notedb = FirebaseDatabase.getInstance("https://pam9-d569e-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users").child(userId).child("notes")

        notedb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noteList.clear()
                for (noteSnapshot in snapshot.children) {
                    val note = noteSnapshot.getValue(Note::class.java)
                    note?.id = noteSnapshot.key
                    if (note != null) {
                        noteList.add(note)
                    }
                }
                noteAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@InsertNoteActivity, "Failed to load notes.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            tvEmail!!.text = currentUser.email
            tvUid!!.text = currentUser.uid
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_keluar -> logOut()
            R.id.btn_submit -> submitData()
        }
    }

    private fun submitData() {
        if (!validateForm()) {
            return
        }
        val title = etTitle?.text.toString()
        val desc = etDesc?.text.toString()
        val id = notedb.push().key
        val note = Note(
            id = id,
            judul = title,
            deskripsi = desc
        )
        notedb.child(id!!).setValue(note).addOnSuccessListener {
            Toast.makeText(this@InsertNoteActivity, "Note added", Toast.LENGTH_SHORT).show()
            etTitle?.text?.clear()
            etDesc?.text?.clear()
        }.addOnFailureListener {
            Toast.makeText(this@InsertNoteActivity, "Failed to add note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(etTitle?.text.toString())) {
            etTitle?.error = "Required"
            result = false
        } else {
            etTitle?.error = null
        }
        if (TextUtils.isEmpty(etDesc?.text.toString())) {
            etDesc?.error = "Required"
            result = false
        } else {
            etDesc?.error = null
        }
        return result
    }

    private fun logOut() {
        mAuth!!.signOut()
        val intent = Intent(this@InsertNoteActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
