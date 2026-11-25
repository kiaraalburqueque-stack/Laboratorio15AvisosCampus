package com.alburqueque.avisocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alburqueque.avisocampus.data.AvisosRepository
import com.alburqueque.avisocampus.model.Aviso
import com.alburqueque.avisocampus.model.Origen
import com.alburqueque.avisocampus.model.PermisoUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Decide qué avisar y lo anota en el Repository. Nunca toca el sistema de notificaciones. */
class AvisosViewModel(private val repo: AvisosRepository) : ViewModel() {

    val avisos: StateFlow<List<Aviso>> = repo.avisos

    // Evento de una sola vez: "muestra esta notificación".
    // El ViewModel no muestra nada: la pantalla recibe el evento y llama al Notificador.
    private val _eventos = MutableSharedFlow<Aviso>()
    val eventos: SharedFlow<Aviso> = _eventos.asSharedFlow()

    private val _permiso = MutableStateFlow<PermisoUi>(PermisoUi.SinPedir)
    val permiso: StateFlow<PermisoUi> = _permiso.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Recordatorio: segundos que faltan, o null si no hay uno activo
    private var trabajo: Job? = null
    private val _faltan = MutableStateFlow<Int?>(null)
    val faltan: StateFlow<Int?> = _faltan.asStateFlow()

    // Receptor del modo avión: la pantalla lo registra solo mientras esto sea true
    private val _escuchandoAvion = MutableStateFlow(false)
    val escuchandoAvion: StateFlow<Boolean> = _escuchandoAvion.asStateFlow()

    /** La pantalla avisa la respuesta del cuadro de permiso (o que ya estaba concedido). */
    fun permisoRespondido(concedido: Boolean) {
        _permiso.value = if (concedido) PermisoUi.Concedido else PermisoUi.Denegado
    }

    /** Botón Mostrar aviso: se anota en el historial y se pide a la pantalla que lo muestre. */
    fun avisarAhora() {
        viewModelScope.launch {
            val aviso = repo.registrar("Tarea lista", "Tu entrega fue registrada", Origen.BOTON)
            _eventos.emit(aviso)
        }
    }

    /**
     * Cuenta regresiva en viewModelScope: sigue aunque se rote la pantalla o se minimice la app.
     * Si ya hay un recordatorio activo, no hace nada.
     */
    fun recordarEn(segundos: Int = 10) {
        if (trabajo?.isActive == true) return
        trabajo = viewModelScope.launch {
            try {
                for (s in segundos downTo 1) {
                    _faltan.value = s
                    delay(1000)
                }
                val aviso = repo.registrar(
                    "Recordatorio", "Revisa tu laboratorio pendiente", Origen.RECORDATORIO
                )
                _eventos.emit(aviso)
            } finally {
                _faltan.value = null // termine bien o cancelado
            }
        }
    }

    /** Cancela la cuenta regresiva: no se emite ningún aviso. */
    fun cancelarRecordatorio() {
        trabajo?.cancel()
    }

    fun cambiarEscucha(valor: Boolean) {
        _escuchandoAvion.value = valor
    }

    /** Lo llama el receptor (a través de la pantalla) cuando cambia el modo avión. */
    fun alCambiarModoAvion(activo: Boolean) {
        viewModelScope.launch {
            val aviso = repo.registrar(
                "Modo avión", if (activo) "Activado" else "Desactivado", Origen.MODO_AVION
            )
            _eventos.emit(aviso)
        }
    }
}
