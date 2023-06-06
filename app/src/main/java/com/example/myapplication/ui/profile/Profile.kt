package com.example.myapplication.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.MAIN
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentProfileBinding


class Profile : Fragment() {

    companion object {
        fun newInstance() = Profile()
    }

    private lateinit var viewModel: ProfileViewModel

    lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonAbout.setOnClickListener{
            MAIN.navController1.navigate(R.id.action_navigation_profile_to_descriptionFragment2)

        }

        binding.buttonVkusotest.setOnClickListener{
            MAIN.navController1.navigate(R.id.action_navigation_profile_to_startedPage2)
        }

        binding.buttonHelp.setOnClickListener{

            val url = "https://yandex.ru/search/?text=как+сделать+переход+по+ссылке+android+studio+kotlin&lr=51&clid=2270456&src=suggest_Nin"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }




}