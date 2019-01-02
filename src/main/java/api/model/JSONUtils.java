package api.model;

import java.util.Map;

/**
 * A set of utilities for converting model objects to JSON
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class JSONUtils {

    /**
     * Defines objects that may be converted to JSON
     */
    public interface JSONable {

        /**
         * Converts this object to a JSON string
         * @return a JSON string representing the object.
         */
        String asJSON();
    }


    /**
     * Converts an object to JSON notation.
     * @param object the object to convert
     * @return a JSON string representing the object
     */
    public static String toJSON(Object object) {
        // If we have a representation of it, use that.
        if (object instanceof JSONable)
            return ((JSONable) object).asJSON();

        // If it's a 'list', convert it and everything in it.
        if (object instanceof Iterable) {
            StringBuilder list = new StringBuilder("[ ");
            for (Object o: ((Iterable) object)) {
                list.append(toJSON(o));
                list.append(", ");
            }
            list.replace(list.lastIndexOf(","), list.length(), " ]");
            return list.toString();
        }

        // If it's a map, it's basically JSON already...
        if (object instanceof Map) {
            Map<String, ?> map = (Map) object;
            StringBuilder json = new StringBuilder("{ ");
            for( String key: map.keySet()) {
                json.append("\"");
                json.append(key);
                json.append("\" : \"");
                json.append(toJSON(map.get(key)));
                json.append("\", ");
            }
            json.replace(json.lastIndexOf(","),json.length()," }");
        }

        // Everything else is essentially a literal so just give up.
        return object.toString();

    }

}
