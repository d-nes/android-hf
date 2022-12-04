package hu.bme.aut.android.topkqh.destinationsharing.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.topkqh.destinationsharing.data.Post
import hu.bme.aut.android.topkqh.destinationsharing.databinding.CardPostBinding

class PostsAdapter(private val context: Context) :
    ListAdapter<Post, PostsAdapter.PostViewHolder>(itemCallback) {

    private var postList: List<Post> = emptyList()
    private var lastPosition = -1

    class PostViewHolder(binding: CardPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvUser: TextView = binding.tvUser
        val tvDestination: TextView = binding.tvDestination
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PostViewHolder(CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val tmpPost = postList[position]
        holder.tvUser.text = tmpPost.user
        holder.tvDestination.text = tmpPost.destination
    }

    fun addPost(post: Post?) {
        post ?: return

        postList += (post)
        submitList((postList))
    }

    companion object {
        object itemCallback : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }
        }
    }
}