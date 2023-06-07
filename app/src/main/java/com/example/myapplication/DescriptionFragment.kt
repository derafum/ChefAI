package com.example.myapplication

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.FragmentDescriptionBinding

class DescriptionFragment : Fragment() {

    companion object {
        fun newInstance() = DescriptionFragment()
    }

    lateinit var binding: FragmentDescriptionBinding
    private lateinit var viewModel: DescriptionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentDescriptionBinding.inflate(layoutInflater, container, false)
        // Получите ссылку на объект ActionBar из активности
        val actionBar = (activity as AppCompatActivity).supportActionBar

        // Установите новый заголовок фрагмента
        actionBar?.title = "Description"
        // Скрыть стрелку "назад" из ActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DescriptionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}