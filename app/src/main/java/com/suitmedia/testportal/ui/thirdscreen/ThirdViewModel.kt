package com.suitmedia.testportal.ui.thirdscreen

import androidx.lifecycle.ViewModel
import com.suitmedia.testportal.repository.UserRepository

class ThirdViewModel(private val userRepository: UserRepository) : ViewModel() {
    fun getUsers(page: Int, perPage: Int) = userRepository.getUsers(page, perPage)
}