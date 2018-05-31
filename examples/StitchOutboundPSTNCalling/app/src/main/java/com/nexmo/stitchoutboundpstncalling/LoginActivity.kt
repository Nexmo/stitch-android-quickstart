package com.nexmo.stitchoutboundpstncalling

import android.content.Intent
import android.os.Bundle
import com.nexmo.sdk.conversation.client.ConversationClient
import com.nexmo.sdk.conversation.client.User
import com.nexmo.sdk.conversation.client.event.NexmoAPIError
import com.nexmo.sdk.conversation.client.event.RequestHandler
import com.nexmo.stitchoutboundpstncalling.utils.Stitch
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(), RequestHandler<User> {
    val JWT = ""

    private lateinit var client: ConversationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        client = Stitch.getInstance(this).conversationClient


        loginBtn.setOnClickListener {
            login()
        }

    }

    private fun login() {
        client.login(JWT, this)
    }

    override fun onError(apiError: NexmoAPIError?) {
        logAndShow(apiError?.message)
    }

    override fun onSuccess(result: User?) {
        startActivity(
                Intent(this, CallActivity::class.java)
        )
    }
}
