package com.alburqueque.avisocampus.notificacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/** Todo lo que toca el sistema de notificaciones vive aquí, nunca en el ViewModel. */
object Notificador {
    const val CANAL = "avisos"

    /** Android 8+ exige un canal. Crearlo varias veces no duplica nada. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL, "Avisos", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }
}
