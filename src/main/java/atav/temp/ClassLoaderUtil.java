package atav.temp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ClassLoaderUtil {

    /**
    * Load a given resource.
    *
    * This method will try to load the resource using the following methods (in order):
    * <ul>
    *  <li>From Thread.currentThread().getContextClassLoader()
    *  <li>From ClassLoaderUtil.class.getClassLoader()
    *  <li>callingClass.getClassLoader()
    * </ul>
    *
    * @param resourceName The name IllegalStateException("Unable to call ")of the resource to load
    * @param callingClass The Class object of the calling object
    */
    public static URL getResource(String resourceName, Class callingClass) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        if (url == null) {
            url = ClassLoaderUtil.class.getClassLoader().getResource(resourceName);
        }

        if (url == null) {
            ClassLoader cl = callingClass.getClassLoader();

            if (cl != null) {
                url = cl.getResource(resourceName);
            }
        }

        if ((url == null) && (resourceName != null) && ((resourceName.length() == 0) || (resourceName.charAt(0) != '/'))) { 
            return getResource('/' + resourceName, callingClass);
        }

        return url;
    }

    /**
    * This is a convenience method to load a resource as a stream.
    *
    * The algorithm used to find the resource is given in getResource()
    *
    * @param resourceName The name of the resource to load
    * @param callingClass The Class object of the calling object
    */
    public static InputStream getResourceAsStream(String resourceName, Class callingClass) {
        URL url = getResource(resourceName, callingClass);

        try {
            return (url != null) ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
    * Load a class with a given name.
    *
    * It will try to load the class in the following order:
    * <ul>
    *  <li>From Thread.currentThread().getContextClassLoader()
    *  <li>Using the basic Class.forName()
    *  <li>From ClassLoaderUtil.class.getClassLoader()
    *  <li>From the callingClass.getClassLoader()
    * </ul>
    *
    * @param className The name of the class to load
    * @param callingClass The Class object of the calling object
    * @throws ClassNotFoundException If the class cannot be found anywhere.
    */
    public static Class loadClass(String className, Class callingClass) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                try {
                    return ClassLoaderUtil.class.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException exc) {
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
        }
    }
}