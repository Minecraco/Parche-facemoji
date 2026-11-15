package app.parchefacemoji.extension.facemoji.settings;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

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
     * Se llama desde los dos fragments de ajustes de Facemoji que usan preferencias: el
     * principal (`SettingsMainFragment`, el que se ve al abrir Ajustes) y el avanzado.
     *
     * @param fragmento el fragment de Facemoji (se recibe como `Object` para no obligar al
     *                  parche a conocer su tipo exacto).
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

            insertarLaPrimera(pantalla, fila);
        } catch (Throwable error) {
            Log.w(TAG, "No se pudo anadir la entrada de ajustes del parche", error);
        }
    }

    /**
     * Coloca la fila como la primera de la lista.
     *
     * `PreferenceGroup` solo ofrece `addPreference`, que anyade al final. Para que aparezca arriba
     * del todo se anyade primero (asi el grupo la engancha y se registra bien) y despues se mueve
     * al principio de su lista interna por reflexion. Si algo de eso falla, la fila se queda al
     * final en vez de perderse.
     */
    private static void insertarLaPrimera(PreferenceGroup grupo, Preference fila) {
        grupo.addPreference(fila);

        try {
            Field campoLista = PreferenceGroup.class.getDeclaredField("mPreferenceList");
            campoLista.setAccessible(true);

            Object valor = campoLista.get(grupo);
            if (!(valor instanceof List)) return;

            @SuppressWarnings("unchecked")
            List<Preference> lista = (List<Preference>) valor;

            if (lista.remove(fila)) {
                lista.add(0, fila);

                // Avisar al grupo de que la lista ha cambiado para que se vuelva a pintar.
                Method avisar = Preference.class.getDeclaredMethod("notifyChanged");
                avisar.setAccessible(true);
                avisar.invoke(grupo);
            }
        } catch (Throwable error) {
            Log.w(TAG, "No se pudo poner la entrada la primera; se queda al final", error);
        }
    }
}
