package com.ultrawork.notes.data.remote

class ApiException(val code: Int, message: String) : Exception(message)
