package com.ssafy.chocopick.ui.mypage

import androidx.fragment.app.Fragment
import com.ssafy.chocopick.R

fun Fragment.navigateTo(fragment: Fragment, tag: String) {
    requireActivity().supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment, tag)
        .addToBackStack(tag)
        .commit()
}

fun Fragment.goBack() {
    requireActivity().supportFragmentManager.popBackStack()
}
