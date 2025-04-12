package com.example.projecttemplateexample

import com.example.projecttemplateexample.models.UserDto

interface UserDataService {
    suspend fun getUsers(): List<UserDto>
}