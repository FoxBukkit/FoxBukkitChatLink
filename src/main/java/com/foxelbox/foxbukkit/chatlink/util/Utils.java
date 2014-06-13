/**
 * This file is part of FoxBukkitChatLink.
 *
 * FoxBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxbukkit.chatlink.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String concat(String separator, Collection<String> parts, int start, String defaultText) {
        // TODO: optimize
        return concatArray(separator, parts.toArray(new String[parts.size()]), start, defaultText);
    }

    public static String concatArray(String separator, String[] array, int start, String defaultText) {
        if (array.length <= start)
            return defaultText;

        if (array.length <= start + 1)
            return array[start]; // optimization

        StringBuilder ret = new StringBuilder(array[start]);
        for(int i = start + 1; i < array.length; i++) {
            ret.append(separator);
            ret.append(array[i]);
        }
        return ret.toString();
    }

    public static <T> List<Class<? extends T>> getSubClasses(Class<T> baseClass, String packageName) {
        final List<Class<? extends T>> ret = new ArrayList<>();
        final File file;
        try {
            final ProtectionDomain protectionDomain = baseClass.getProtectionDomain();
            final CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource == null)
                return ret;

            final URL location = codeSource.getLocation();
            final URI uri = location.toURI();
            file = new File(uri);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return ret;
        }
        final String[] fileList;

        if (file.isDirectory() || (file.isFile() && !file.getName().endsWith(".jar"))) {
            String packageFolderName = "/"+packageName.replace('.','/');

            URL url = baseClass.getResource(packageFolderName);
            if (url == null)
                return ret;

            File directory = new File(url.getFile());
            if (!directory.exists())
                return ret;

            // Get the list of the files contained in the package
            fileList = directory.list();
        }
        else if (file.isFile()) {
            final List<String> tmp = new ArrayList<>();
            final JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            }
            catch (IOException e) {
                e.printStackTrace();
                return ret;
            }

            Pattern pathPattern = Pattern.compile(packageName.replace('.','/')+"/(.+\\.class)");
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                Matcher matcher = pathPattern.matcher(entries.nextElement().getName());
                if (!matcher.matches())
                    continue;

                tmp.add(matcher.group(1));
            }

            fileList = tmp.toArray(new String[tmp.size()]);
        }
        else {
            return ret;
        }

        Pattern classFilePattern = Pattern.compile("(.+)\\.class");
        for (String fileName : fileList) {
            // we are only interested in .class files
            Matcher matcher = classFilePattern.matcher(fileName);
            if (!matcher.matches())
                continue;

            // removes the .class extension
            String classname = matcher.group(1);
            try {
                final String qualifiedName = packageName+"."+classname.replace('/', '.');
                final Class<?> classObject = Class.forName(qualifiedName);
                final Class<? extends T> classT = classObject.asSubclass(baseClass);

                // Try to create an instance of the object
                ret.add(classT);
            }
            catch (ClassCastException e) {
                //noinspection UnnecessaryContinue
                continue;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
}
