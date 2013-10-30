package uk.ac.ebi.pride.proteomes.web.client.utils;

import java.util.HashMap;

/**
 * A Map with a get method that returns a default value if the key is not
 * contained in the map.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 */
public class DefaultHashMap<K,V> extends HashMap<K,V> {
    public V get(K key, V defaultValue) {
        if(containsKey(key)) {
            return get(key);
        }

        return defaultValue;
    }
}
