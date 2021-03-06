package com.abdoul.chatapplication

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.abdoul.chatapplication.service.MyFirebaseMessagingService
import com.abdoul.chatapplication.util.FireStoreUtil
import com.abdoul.chatapplication.util.CommonUtils
import com.abdoul.customsnackbar.CustomSnackBar
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    private lateinit var dialog: Dialog
    private val SIGN_IN_REQUEST = 1
    private val signInProviders = listOf(
        AuthUI.IdpConfig.EmailBuilder()
            .setAllowNewAccounts(true)
            .setRequireName(true)
            .build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        btnSignIn.setOnClickListener {
            if(CommonUtils.isOnline(this)){
                val intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(signInProviders)
                    .setLogo(R.drawable.ic_logo)
                    .build()
                startActivityForResult(intent, SIGN_IN_REQUEST)
            }else{
                CommonUtils.showSnackBar(
                    clParentView, getString(R.string.no_internet),
                    R.color.grey,
                    R.drawable.ic_no_network, this
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                showProgressDialog(true)
                FireStoreUtil.initCurrentUserIfFirstTime {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                        MyFirebaseMessagingService.addTokenToFirestore(it.token)
                    }
                    showProgressDialog(false)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) return
                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK -> {
                        CommonUtils.showSnackBar(
                            clParentView, getString(R.string.no_internet),
                            R.color.red,
                            R.drawable.ic_no_network, this
                        )
                    }
                    ErrorCodes.UNKNOWN_ERROR -> {
                        CommonUtils.showSnackBar(
                            clParentView, getString(R.string.unknown_error),
                            R.color.red,
                            R.drawable.ic_error, this
                        )
                    }
                }
            }
        }
    }

    private fun showProgressDialog(show: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress_dialog)
        dialog = builder.create()
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}