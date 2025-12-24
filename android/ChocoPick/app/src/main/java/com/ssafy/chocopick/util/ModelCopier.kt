package com.ssafy.chocopick.util

import android.content.Context
import java.io.File

object ModelCopier {

    private const val ASSET_PATH = "models/gemma3-1b-it-int4.litertlm"
    private const val OUT_NAME = "gemma3-1b-it-int4.litertlm"

    fun copyIfNeeded(context: Context): File {
        val out = File(context.filesDir, OUT_NAME)
        if (out.exists() && out.length() > 0) return out

        context.assets.open(ASSET_PATH).use { input ->
            out.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return out
    }
}
