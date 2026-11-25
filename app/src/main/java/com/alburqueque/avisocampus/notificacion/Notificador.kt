package com.alburqueque.avisocampus.notificacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alburqueque.avisocampus.R
import com.alburqueque.avisocampus.model.Aviso

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

    /** Antes de Android 13 no hace falta pedir permiso: se considera concedido. */
    fun tienePermiso(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Muestra el aviso como notificación. Sin permiso devuelve false (sin lanzar error)
     * para que la pantalla le avise al ViewModel.
     */
    @SuppressLint("MissingPermission") // el permiso se comprueba en la primera línea
    fun mostrar(context: Context, aviso: Aviso): Boolean {
        if (!tienePermiso(context)) return false

        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // El id del aviso hace que cada notificación sea distinta (no se reemplazan)
        NotificationManagerCompat.from(context).notify(aviso.id, notificacion)
        return true
    }
}
