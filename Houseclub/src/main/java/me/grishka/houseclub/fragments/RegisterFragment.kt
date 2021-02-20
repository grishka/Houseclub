package me.grishka.houseclub.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import me.grishka.appkit.Nav
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import me.grishka.houseclub.R
import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.methods.UpdateName
import me.grishka.houseclub.api.methods.UpdateUsername

class RegisterFragment : BaseToolbarFragment() {
    private var firstNameInput: EditText? = null
    private var lastNameInput: EditText? = null
    private var usernameInput: EditText? = null
    private var nextBtn: Button? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        setTitle(R.string.register)
    }

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View {
        val view = inflater.inflate(R.layout.register, container, false)
        firstNameInput = view.findViewById(R.id.first_name_input)
        lastNameInput = view.findViewById(R.id.last_name_input)
        usernameInput = view.findViewById(R.id.username_input)
        nextBtn = view.findViewById(R.id.next)
        nextBtn?.setOnClickListener(View.OnClickListener { v: View -> onNextClick(v) })
        return view
    }

    private fun onNextClick(v: View) {
        val first = firstNameInput!!.text.toString()
        val last = lastNameInput!!.text.toString()
        val username = usernameInput!!.text.toString()
        if (first.length < 2 || last.length < 2 || username.length < 2) {
            Toast.makeText(activity, R.string.all_fields_are_required, Toast.LENGTH_SHORT).show()
            return
        }
        if (username.length > 16) {
            Toast.makeText(activity, R.string.username_limit, Toast.LENGTH_SHORT).show()
            return
        }
        UpdateName("$first $last")
            .wrapProgress(activity)
            .setCallback(object : Callback<BaseResponse?> {
                override fun onSuccess(result: BaseResponse?) {
                    UpdateUsername(username)
                        .wrapProgress(activity)
                        .setCallback(object : Callback<BaseResponse?> {
                            override fun onSuccess(result: BaseResponse?) {
                                Toast.makeText(activity, R.string.welcome_to_clubhouse, Toast.LENGTH_SHORT).show()
                                Nav.goClearingStack(activity, HomeFragment::class.java, null)
                            }

                            override fun onError(error: ErrorResponse) {
                                error.showToast(activity)
                            }
                        })
                        .exec()
                }

                override fun onError(error: ErrorResponse) {
                    error.showToast(activity)
                }
            })
            .exec()
    }
}