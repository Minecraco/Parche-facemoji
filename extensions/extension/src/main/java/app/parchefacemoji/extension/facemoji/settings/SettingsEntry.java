package app.parchefacemoji.extension.facemoji.settings;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * Añade la fila "Ajustes de Minecraco" dentro de los ajustes de Facemoji.
 *
 * El parche inyecta una llamada a {@link #agregarEntrada(Object)} al principio de
 * `SettingsAdvancedFragment.onResume()`, que es un `android.preference.PreferenceFragment`:
 * a esas alturas la pantalla de preferencias ya esta creada, asi que basta con anadir una
 * `Preference` mas a la lista.
 *
 * Todo va envuelto en `try/catch`: si una version futura cambia esa pantalla, la app tiene
 * que seguir abriendo los ajustes con normalidad aunque nuestra fila no aparezca.
 */
@SuppressWarnings("deprecation")
public final class SettingsEntry {

    private static final String TAG = "ParcheFacemoji";

    /** Clave de la preferencia, para no anadirla dos veces. */
    public static final String CLAVE = "parche_facemoji_ajustes";

    private SettingsEntry() {
    }

    /**
     * Punto de inyeccion: anade la fila a la pantalla de ajustes recibida.
     *
     * @param fragmento el `SettingsAdvancedFragment` de Facemoji (se recibe como `Object`
     *                  para no obligar al parche a conocer su tipo exacto).
     */
    public static void agregarEntrada(Object fragmento) {
        if (!(fragmento instanceof PreferenceFragment)) return;

        try {
            PreferenceFragment ajustes = (PreferenceFragment) fragmento;
            PreferenceScreen pantalla = ajustes.getPreferenceScreen();
            if (pantalla == null) return;

            // Si ya esta puesta, no se duplica (onResume se llama varias veces).
            if (pantalla.findPreference(CLAVE) != null) return;

            final Context contexto = ajustes.getActivity();
            if (contexto == null) return;

            PatchPreferences.iniciar(contexto);

            Preference fila = new Preference(contexto);
            fila.setKey(CLAVE);
            fila.setTitle("Ajustes Minecraco");
            fila.setSummary("Anuncios, VIP y rastreo");
            fila.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preferencia) {
                    try {
                        Intent intento = new Intent(
                                preferencia.getContext(),
                                SettingsActivity.class
                        );
                        intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        preferencia.getContext().startActivity(intento);
                    } catch (Throwable error) {
                        Log.w(TAG, "No se pudieron abrir los ajustes del parche", error);
                    }
                    return true;
                }
            });

            pantalla.addPreference(fila);
        } catch (Throwable error) {
            Log.w(TAG, "No se pudo anadir la entrada de ajustes del parche", error);
        }
    }
}
