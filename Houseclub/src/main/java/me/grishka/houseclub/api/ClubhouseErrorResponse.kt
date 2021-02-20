package me.grishka.houseclub.api

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import me.grishka.appkit.api.ErrorResponse
import me.grishka.houseclub.BuildConfig
import me.grishka.houseclub.R

class ClubhouseErrorResponse : ErrorResponse {
    private var message: String

    constructor(code: Int, message: String) {
        this.message = message
    }

    constructor(br: BaseResponse) {
        message = br.errorMessage!!
    }

    override fun bindErrorView(view: View) {
        val txt = view.findViewById<TextView>(R.id.error_text)
        if (BuildConfig.DEBUG) txt.text = """
     ${view.context.getString(R.string.error_loading)}:
     $message
     """.trimIndent() else txt.setText(R.string.error_loading)
    }

    override fun showToast(context: Context) {
        if (BuildConfig.DEBUG) Toast.makeText(context, message, Toast.LENGTH_LONG).show() else Toast.makeText(
            context,
            R.string.error_loading,
            Toast.LENGTH_SHORT
        ).show()
    }
}