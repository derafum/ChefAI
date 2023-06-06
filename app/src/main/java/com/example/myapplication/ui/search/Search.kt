package com.example.myapplication.ui.search

import android.content.ClipData
import android.content.ClipData.Item
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.FragmentSearchBinding

class Search : Fragment() {

    companion object {
        fun newInstance() = Search()
    }

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchView: SearchView
    private lateinit var reView3: RecyclerView
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*

        searchView = binding.searchView

        searchView.clearFocus()



        searchView.setOnQueryTextListener{object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterList(newText)
                return true;
            }

        }}



        reView3 = binding.reView3
        reView3.setHasFixedSize(true)
        binding.reView3.layoutManager = LinearLayoutManager(context)
        reView3.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
*/


        return inflater.inflate(R.layout.fragment_search, container, false)
    }
/*
    private fun filterList(text: String) {
        List<Item> filteredList = new ArrayList<>();
        for (Item item: itemList){
            if (item.getItemName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()){
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();

        }else{
            itemAdapter.setFilteredList(filteredList);
        }

    }

 */

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        // TODO: Use the ViewModel
    }

}