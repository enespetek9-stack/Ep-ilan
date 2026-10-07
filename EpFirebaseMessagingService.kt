package com.epilan.app
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

class EpFirebaseMessagingService:FirebaseMessagingService(){
 override fun onNewToken(token:String){ super.onNewToken(token); /* Supabase device_tokens endpoint'e token gönder */ }
 override fun onMessageReceived(message:RemoteMessage){
  val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(NotificationChannel("ilan","EP İlan",NotificationManager.IMPORTANCE_HIGH))
  val n=NotificationCompat.Builder(this,"ilan").setSmallIcon(com.epilan.app.R.drawable.ic_launcher).setContentTitle(message.notification?.title ?: "Yeni EP İlan").setContentText(message.notification?.body ?: "Yeni kamu ilanı bulundu.").setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build()
  nm.notify(System.currentTimeMillis().toInt(),n)
 }
}
