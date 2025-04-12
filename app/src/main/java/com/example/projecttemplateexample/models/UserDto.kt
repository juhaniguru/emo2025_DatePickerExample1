package com.example.projecttemplateexample.models

data class UserDto(val id: Int, val name: String, val username: String, val email: String)

data class UserState(
    // loading on boolean, jonka mukaan näytetään käyttäjälle lataus-spinner
    // jos loading = true -> näytetään CircularProgressIndicator
    val loading: Boolean = false,
    // users on lista käyttäjiä, joiden haku netistä simuloidaan
    val users: List<UserDto> = listOf(),
    // jos haussa tulee virhe, näytetään se käyttäjälle
    val error: String? = null
)
