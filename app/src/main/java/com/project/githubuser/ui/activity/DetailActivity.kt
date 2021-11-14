package com.project.githubuser.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.githubuser.R
import com.project.githubuser.adapter.SectionsPagerAdapter
import com.project.githubuser.databinding.ActivityDetailBinding
import com.project.githubuser.model.User
import com.project.githubuser.viewModel.DetailActViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailActivity: AppCompatActivity() {

    private lateinit var detailActViewModel: DetailActViewModel
    private lateinit var binding: ActivityDetailBinding

    var id: Int = 0

    companion object {
        private val TAB_TITLES = intArrayOf(
            R.string.tab_following,
            R.string.tab_follower
        )
        const val EXTRA_DETAIL = "extra_detail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = intent.getParcelableExtra<User>(EXTRA_DETAIL)
        val username = user?.login.toString()
        val avatarUrl = user?.avatar_url.toString()

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        sectionsPagerAdapter.username = user?.login
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()

        supportActionBar?.hide()
        binding.btnBack.setOnClickListener{ finish() }

        detailActViewModel = ViewModelProvider(this).get(DetailActViewModel::class.java)

        detailActViewModel.setDetail(username)
        detailActViewModel.userDetail.observe(this, { users ->
            Glide.with(this).load(users.avatar_url).circleCrop().into(binding.imgAvatar)
            binding.tvTextName.text = users.name
            binding.tvTextUsername.text = users.login
            binding.includeDescription.tvTextCompany.text = users.company
            binding.includeDescription.tvTextLocation.text = users.location
            binding.includeDescription.tvTextRepo.text = users.public_repos.toString()
        })

        detailActViewModel.isLoading.observe(this, {
            showLoading(it)
        })

       var isCheck = false
        CoroutineScope(Dispatchers.IO).launch {
            val count = detailActViewModel.checkUser(id)
            withContext(Dispatchers.Main) {
                if (count != null) {
                    if (count>0) {
                        binding.includeDescription.favoriteIcon.isChecked = true
                        isCheck = true
                    } else {
                        binding.includeDescription.favoriteIcon.isChecked = false
                        isCheck = false
                    }
                }
            }
        }

        binding.includeDescription.favoriteIcon.setOnClickListener {
            isCheck = !isCheck
            if (isCheck) {
                detailActViewModel.addToFavorite(username, id, avatarUrl)
                Toast.makeText(this@DetailActivity,"Added to Favorites", Toast.LENGTH_SHORT).show()
            } else {
                detailActViewModel.removeFromFavorite(id)
                Toast.makeText(this@DetailActivity,"Removed from Favorites", Toast.LENGTH_SHORT).show()
            }
            binding.includeDescription.favoriteIcon.isChecked = isCheck
        }

        binding.includeDescription.shareIcon.setOnClickListener {
            val sendIntent : Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Github User : \n${user?.html_url} ")
                type = "text/html"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Github User")
            startActivity(shareIntent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}