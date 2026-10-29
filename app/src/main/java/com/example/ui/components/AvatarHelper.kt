package com.example.ui.components

import androidx.annotation.DrawableRes
import com.example.R

object AvatarHelper {
    @DrawableRes
    fun getAvatarDrawable(name: String): Int {
        return when (name.lowercase()) {
            "avatar_elena" -> R.drawable.avatar_elena
            "avatar_julian" -> R.drawable.avatar_julian
            "avatar_isaac" -> R.drawable.avatar_isaac
            "avatar_zara" -> R.drawable.avatar_zara
            else -> R.drawable.avatar_elena
        }
    }
}
