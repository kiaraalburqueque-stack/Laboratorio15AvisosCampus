package com.alburqueque.avisocampus.notificacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alburqueque.avisocampus.MainActivity
import com.alburqueque.avisocampus.R
import com.alburqueque.avisocampus.model.Aviso
import com.alburqueque.avisocampus.model.Origen

/** Todo lo que toca el sistema de notificaciones vive aquí, nunca en el ViewModel. */
object Notificador {
    const val CANAL = "avisos"
    const val CANAL_URGENTE = "urgentes"

    /**
     * Android 8+ exige canales. Dos canales: «Avisos» (normal) y «Urgentes» (IMPORTANCE_HIGH:
     * aparece emergente arriba de la pantalla). Crearlos varias veces no duplica nada.
     */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val avisos = NotificationChannel(CANAL, "Avisos", NotificationManager.IMPORTANCE_DEFAULT)
            val urgentes = NotificationChannel(
                CANAL_URGENTE, "Urgentes", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Recordatorios que aparecen emergentes" }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(avisos, urgentes))
        }
    }

    /** El recordatorio va por el canal urgente; el resto, por el normal. */
    private fun canalDe(aviso: Aviso): String =
        if (aviso.origen == Origen.RECORDATORIO) CANAL_URGENTE else CANAL

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

        // Al tocar la notificación se abre la app. SINGLE_TOP + CLEAR_TOP reutiliza la pantalla
        // si ya estaba abierta (no se pierde el historial). FLAG_IMMUTABLE: nadie puede cambiar el Intent.
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val urgente = aviso.origen == Origen.RECORDATORIO
        val notificacion = NotificationCompat.Builder(context, canalDe(aviso))
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            // En Android 7 la importancia la da la prioridad (no hay canales)
            .setPriority(
                if (urgente) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendiente)
            .setAutoCancel(true) // se quita al tocarla
            .build()

        // El id del aviso hace que cada notificación sea distinta (no se reemplazan)
        NotificationManagerCompat.from(context).notify(aviso.id, notificacion)
        return true
    }
}
