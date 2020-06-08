package com.abdoul.chatapplication.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdoul.chatapplication.ChatActivity
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.model.item.PersonItem
import com.abdoul.chatapplication.util.AppConstants
import com.abdoul.chatapplication.util.FireStoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment() {

    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var peopleSection: Section

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        activity?.let {
            userListenerRegistration = FireStoreUtil.addUsersListener(it, this::updateRecyclerView)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FireStoreUtil.removeListener(userListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun updateRecyclerView(items: List<Item>) {

        fun init() {
            chatRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@ChatFragment.context)
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    peopleSection = Section(items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = peopleSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is PersonItem) {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra(AppConstants.USER_NAME, item.person.name)
            intent.putExtra(AppConstants.USER_ID, item.userId)
            startActivity(intent)
        }
    }
}