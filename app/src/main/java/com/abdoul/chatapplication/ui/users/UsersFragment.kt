package com.abdoul.chatapplication.ui.users

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdoul.chatapplication.R
import com.abdoul.chatapplication.model.item.PersonItem
import com.abdoul.chatapplication.ui.chat.ChatActivity
import com.abdoul.chatapplication.util.AppConstants
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.fragment_chat.*

class UsersFragment : Fragment() {

    private var shouldInitRecyclerView = true
    private lateinit var peopleSection: Section
    lateinit var usersViewModel: UsersViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)

        usersViewModel = ViewModelProvider(this).get(UsersViewModel::class.java)
        activity?.let {
            usersViewModel.initUserListener(it)
        }

        usersViewModel.itemsLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { items ->
                updateRecyclerView(items)
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usersViewModel.removeUserListenerRegistration()
        shouldInitRecyclerView = true
    }

    private fun updateRecyclerView(items: List<Item>) {

        fun init() {
            chatRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@UsersFragment.context)
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

    private val onItemClick = OnItemClickListener { item, _ ->
        if (item is PersonItem) {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra(AppConstants.USER_NAME, item.person.name)
            intent.putExtra(AppConstants.USER_ID, item.userId)
            startActivity(intent)
        }
    }
}