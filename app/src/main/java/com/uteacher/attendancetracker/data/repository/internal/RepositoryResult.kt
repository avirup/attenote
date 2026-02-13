package com.uteacher.attendancetracker.data.repository.internal

sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String) : RepositoryResult<Nothing>()
}

fun <T> RepositoryResult<T>.getOrNull(): T? = when (this) {
    is RepositoryResult.Success -> data
    is RepositoryResult.Error -> null
}

fun <T> RepositoryResult<T>.getOrThrow(): T = when (this) {
    is RepositoryResult.Success -> data
    is RepositoryResult.Error -> throw IllegalStateException(message)
}
