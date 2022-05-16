package com.project.githubuser.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var login: String?,
    var name: String?,
    var avatar_url: String?,
    var location: String?,
    var company: String?,
    var public_repos: String?,
    var html_url: String?
) : Parcelable