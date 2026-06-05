package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("android.provider.Telephony.SMS_RECEIVED" == intent.action) {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as? Array<*>
                if (pdus != null) {
                    for (pdu in pdus) {
                        val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                        val messageBody = sms.messageBody
                        
                        // Check if the message is from your app's panic system
                        if (messageBody.startsWith("APP_PANIC_ALERT")) {
                            // Stop the SMS from reaching the default messaging inbox
                            this.abortBroadcast() 
                            
                            // Wake up the device and start the real-time alarm service
                            val serviceIntent = Intent(context, AlarmService::class.java)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                        }
                    }
                }
            }
        }
    }
}
