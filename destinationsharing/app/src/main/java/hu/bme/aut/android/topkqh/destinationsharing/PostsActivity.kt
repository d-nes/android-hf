package hu.bme.aut.android.topkqh.destinationsharing

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.topkqh.destinationsharing.adapter.PostsAdapter
import hu.bme.aut.android.topkqh.destinationsharing.data.Post
import hu.bme.aut.android.topkqh.destinationsharing.databinding.ActivityPostsBinding
import hu.bme.aut.android.topkqh.destinationsharing.auth.Firebase as Firebase1

class PostsActivity : Firebase1() {

    private lateinit var binding: ActivityPostsBinding
    private lateinit var postsAdapter: PostsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarPosts.toolbar)

        binding.appBarPosts.fab.setOnClickListener {
            NewFragment().show(
                supportFragmentManager,
                NewFragment.TAG
            )
        }

        postsAdapter = PostsAdapter(applicationContext)
        binding.appBarPosts.contentPosts.rvPosts.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        binding.appBarPosts.contentPosts.rvPosts.adapter = postsAdapter

        title = "Logged in as: " + userName

        initPostsListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.posts, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if(id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else if(id == R.id.action_settings) {

        }

        return super.onOptionsItemSelected(item)
    }

    private fun initPostsListener() {
        val db = Firebase.firestore
        db.collection("posts")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> postsAdapter.addPost(dc.document.toObject(Post::class.java))
                        DocumentChange.Type.MODIFIED -> {}
                        //DocumentChange.Type.REMOVED -> Toast.makeText(this, dc.document.data.toString(), Toast.LENGTH_SHORT).show()
                        //DocumentChange.Type.REMOVED -> Toast.makeText(this, "A share has stopped", Toast.LENGTH_SHORT).show()
                        DocumentChange.Type.REMOVED -> postsAdapter.deletePost(dc.document.toObject(Post::class.java))
                    }
                }
            }
    }

    override fun onResume() {
        postsAdapter.notifyDataSetChanged()
        super.onResume()
    }
}