package com.zelgius.api


sealed class Result<T> {
    class Success<T>(val value: T) : Result<T>()
    class Error<T>(val error: Throwable) : Result<T>()
}