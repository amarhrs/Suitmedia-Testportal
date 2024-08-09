package com.suitmedia.testportal.ui.thirdscreen

import android.content.Intent
import android.os.Bundle
import com.suitmedia.testportal.data.Result
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.suitmedia.testportal.R
import com.suitmedia.testportal.adapter.LoadingStateAdapter
import com.suitmedia.testportal.adapter.UserAdapter
import com.suitmedia.testportal.data.remote.response.DataItem
import com.suitmedia.testportal.databinding.ActivityThirdBinding
import com.suitmedia.testportal.ui.ViewModelFactory

class ThirdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThirdBinding
    private lateinit var viewModel: ThirdViewModel
    private var selectedDataItem: DataItem? = null
    private var currentPage = 1
    private val perPage = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolBar()

        binding.ivBack.setOnClickListener {
            finish()
        }
        val factory: ViewModelFactory = ViewModelFactory.getInstance()
        viewModel = ViewModelProvider(this, factory)[ThirdViewModel::class.java]

        listUsers()
    }

    private fun setToolBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.show()
        supportActionBar?.title = ""
    }

    private fun listUsers() {
        val userAdapter = UserAdapter()
        val layout = LinearLayoutManager(this)
        binding.rvItem.apply {
            layoutManager = layout
            setHasFixedSize(true)
            adapter = userAdapter
            adapter = userAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    userAdapter.retry()
                }
            )
        }

        viewModel.getUsers(currentPage, perPage).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    result.data.observe(this) { pagingData ->
                        userAdapter.submitData(lifecycle, pagingData)
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            viewModel.getUsers(currentPage, perPage).observe(this) { result ->
                when (result) {
                    is Result.Loading -> {
                        binding.refreshLayout.isRefreshing = true
                    }

                    is Result.Success -> {
                        binding.refreshLayout.isRefreshing = false
                        result.data.observe(this) { pagingData ->
                            userAdapter.submitData(lifecycle, pagingData)
                        }
                    }

                    is Result.Error -> {
                        binding.refreshLayout.isRefreshing = false
                        Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        userAdapter.onItemClick = {
            selectedDataItem = it
            val resultIntent = Intent()
            val outState = Bundle()
            outState.putSerializable("selected_user", selectedDataItem)
            resultIntent.putExtras(outState)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}