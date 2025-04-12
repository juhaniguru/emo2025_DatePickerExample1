package com.example.projecttemplateexample

import com.example.projecttemplateexample.models.UserDto

class UserDataServiceImpl(private val api: DataApi) : UserDataService {
    override suspend fun getUsers(): List<UserDto> {
        TODO()
    }
}