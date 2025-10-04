package com.ssba.pantrychef.pantry.data

import com.ssba.pantrychef.data.ApiClient
import retrofit2.Retrofit

private interface PantryApiService {

}
class myCLass{
    val client: Retrofit  by Lazy{
        ApiClient.create("sdfljks")

    }
}