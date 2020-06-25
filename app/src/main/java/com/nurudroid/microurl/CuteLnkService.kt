package com.nurudroid.microurl

import com.nurudroid.microurl.ApiProvider.API_KEY
import com.nurudroid.microurl.models.FetchLinksResponse
import com.nurudroid.microurl.models.LinkResponse
import retrofit2.Call
import retrofit2.http.*

/*
 *********************************************************
 * Created by Ismail Nurudeen on 24-Jun-20 at 8:18 PM.      *
 * Copyright (c) 2020 Nurudroid. All rights reserved.    *
 *********************************************************
 **/

// This contains all our API request methods.
interface CuteLnkService {
    @Headers("Content-Type: application/x-www-form-urlencoded", "Authorization: Bearer $API_KEY")
    @POST("links")
    fun createLink(@Query("url") url: String): Call<LinkResponse>

    @Headers("Accept: application/json", "Authorization: Bearer $API_KEY")
    @GET("links")
    fun fetchAllLinks(): Call<FetchLinksResponse>

    @Headers("Accept: application/json", "Authorization: Bearer $API_KEY")
    @GET("links/{id}")
    fun fetchLink(
        @Path("id") id: Int
    ): Call<LinkResponse>

    @Headers("Content-Type: application/x-www-form-urlencoded", "Authorization: Bearer $API_KEY")
    @PUT("links/{id}")
    fun updateLink(
        @Path("id") id: Int, @Query("url") url: String
    ): Call<LinkResponse>

    @Headers("Authorization: Bearer $API_KEY")
    @DELETE("links/{id}")
    fun deleteLink(
        @Path("id") id: Int
    ): Call<LinkResponse>
}