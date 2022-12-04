package hu.bme.aut.android.topkqh.destinationsharing.adapter

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.topkqh.destinationsharing.data.Post
import hu.bme.aut.android.topkqh.destinationsharing.databinding.CardPostBinding
import java.lang.Double.parseDouble

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
        holder.tvUser.text = tmpPost.user + " is on their way to:"
        val dst = tmpPost.destination?.subSequence(10, tmpPost.destination.lastIndex)?.split(",")
        holder.tvDestination.text = tmpPost.destination?.let {
            Geocoder(this.context).getFromLocation(
                parseDouble(dst?.get(0)),parseDouble(dst?.get(1)), 1).get(0).getAddressLine(0)
        }
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