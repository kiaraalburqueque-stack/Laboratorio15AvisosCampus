package com.alburqueque.avisocampus

import android.app.Application
import com.alburqueque.avisocampus.notificacion.Notificador

/** Se ejecuta una sola vez al iniciar la app, antes que cualquier pantalla. */
class AvisosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notificador.crearCanal(this)
    }
}
