package io.tylerwalker.buyyouadrink.activity.home

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.tylerwalker.buyyouadrink.R
import io.tylerwalker.buyyouadrink.R.drawable.round
import io.tylerwalker.buyyouadrink.model.ListItem
import io.tylerwalker.buyyouadrink.model.ListItemType
import io.tylerwalker.buyyouadrink.util.toBitmap
import io.tylerwalker.buyyouadrink.util.toRounded
import io.tylerwalker.buyyouadrink.util.toRoundedDrawable
import kotlinx.android.synthetic.main.recycler_element.view.*
import kotlinx.android.synthetic.main.recycler_element_header.view.*

class Adapter(val activity: HomeScreen): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listItems: List<ListItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val element = LayoutInflater.from(parent.context)
                        .inflate(R.layout.recycler_element_header, parent, false) as ConstraintLayout
                 HeaderViewHolder(element, activity)
            }
            else -> {
                val element = LayoutInflater.from(parent.context)
                        .inflate(R.layout.recycler_element, parent, false) as ConstraintLayout
                 UserViewHolder(element, activity)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        when (viewType) {
            0 -> {
                holder as HeaderViewHolder
                holder.bind(listItems[position] as ListItem.ListItemHeader)
            }
            else -> {
                holder as UserViewHolder
                holder.bind(listItems[position] as ListItem.UserListItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        listItems[position].type
        return when(listItems[position].type) {
            ListItemType.ListItemHeaderType -> 0
            ListItemType.UserListItemType -> 1
        }
    }

    override fun getItemCount(): Int = listItems.size

    fun updateUsers(items: List<ListItem>?) {
        items?.let {
            listItems = it
            notifyDataSetChanged()
        }
    }

    class HeaderViewHolder(val element: ConstraintLayout, val activity: HomeScreen): RecyclerView.ViewHolder(element) {
        fun bind(listItem: ListItem.ListItemHeader) = with (element) {
            listItem.label!!.let {
                recycler_element_header_text.text = it
            }
        }
    }

    class UserViewHolder(val element: ConstraintLayout, val activity: HomeScreen): RecyclerView.ViewHolder(element) {
        fun bind(listItem: ListItem.UserListItem) = with (element) {

            recycler_element_name_text.text = listItem.user!!.display_name
            recycler_element_caption_text.text = listItem.user.bio
            recycler_element_button.setOnClickListener { activity.transitionToProfile(listItem.user.user_id) }

            listItem.user.profile_image.let {
                if (!it.isEmpty()) {
                    recycler_element_image.setImageBitmap(it.toBitmap()?.toRounded())
                }
            }
        }
    }
}