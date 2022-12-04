package hu.bme.aut.android.topkqh.destinationsharing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.topkqh.destinationsharing.adapter.PostsAdapter
import hu.bme.aut.android.topkqh.destinationsharing.data.Post
import hu.bme.aut.android.topkqh.destinationsharing.databinding.ActivityPostsBinding
import hu.bme.aut.android.topkqh.destinationsharing.location.LocationService
import java.io.File
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
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
            startActivity(Intent(this, SettingsActivity::class.java))
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
                        DocumentChange.Type.REMOVED -> postsAdapter.deletePost(dc.document.toObject(Post::class.java))
                    }
                }
            }
    }

    override fun onResume() {
        postsAdapter.notifyDataSetChanged()
        super.onResume()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        postsAdapter.notifyDataSetChanged()
        super.onActivityReenter(resultCode, data)
    }
}