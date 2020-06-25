package com.nurudroid.microurl.models

/*
 *********************************************************
 * Created by Ismail Nurudeen on 24-Jun-20 at 7:54 PM.      *
 * Copyright (c) 2020 Nurudroid. All rights reserved.    *
 *********************************************************
 **/
data class LinkResponse(
    val data: LinkResponseData,
    val status: Int
)

data class FetchLinksResponse(
    val data: ArrayList<LinkResponseData>,
    val links: PageLink,
    val meta: Meta
)

data class PageLink(
    val first: String = "https://cutelnk.com/api/v1/links?page=1",
    val last: String = "https://cutelnk.com/api/v1/links?page=1",
    val prev: String? = null,
    val next: String? = null
)

data class Meta(
    val current_page: Int = 1,
    val from: Int = 1,
    val last_page: Int = 1,
    val path: String = "https://cutelnk.com/api/v1/links",
    val per_page: Int = 15,
    val to: Int = 3,
    val total: Int = 3
)

data class LinkResponseData(
    val id: Int,
    val alias: String,
    val url: String,
    val short_url: String,
    val title: String?,
    val target_type: Int? = null,
    val geo_target: String? = null,
    val platform_target: String? = null,
    val rotation_target: String? = null,
    val last_rotation: String? = null,
    val disabled: Int? = null,
    val public: Int? = null,
    val expiration_url: String? = null,
    val expiration_clicks: Int? = null,
    val clicks: Int? = null,
    val user_id: Int,
    val space: Int? = null,
    val domain: Int? = null,
    val ends_at: String? = null,
    val created_at: String = "2020-06-24T19:01:35.000000Z",
    val updated_at: String = "2020-06-24T19:01:35.000000Z"
)