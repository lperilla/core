package org.lperilla.framework.core.reflect;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lperilla.framework.core.action.Command;

/**
 * This class finds classes that extend one of a set of parent classes
 *
 */
public final class ClassFinder {

	private static final Logger logger = LogManager.getLogger(ClassFinder.class);

	private static final String JAVA_CLASS_PATH = "java.class.path";

	private static final String DOT_CLASS = ".class";

	private static final transient ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

	// static only
	private ClassFinder() {
	}

	public static List<String> findClassesThatExtend() throws IOException {
		Class<?>[] parentClasses = new Class<?>[] { Command.class };
		return findClassesThatExtend(parentClasses);
	}

	public static List<String> findClassesThatExtend(Class<?>[] classes) throws IOException {
		Set<String> listClasses = new TreeSet<String>();

		if (classes == null)
			new NullPointerException("classes no puede ser nulo");

		final String javaClassPath = System.getProperty(JAVA_CLASS_PATH);
		StringTokenizer stPaths = new StringTokenizer(javaClassPath, File.pathSeparator);

		while (stPaths.hasMoreElements()) {
			findClassesInOnePath(stPaths.nextToken(), classes, listClasses);
		}
		return new ArrayList<String>(listClasses);
	}

	private static void findClassesInOnePath(String strPath, Class<?>[] parentClasses, Set<String> listClasses) throws IOException {
		File file = new File(strPath);
		if (file.isDirectory()) {
			findClassesInPathsDir(strPath, file, parentClasses, listClasses);
		} else if (file.exists()) {
			findClassesInJarFile(file, parentClasses, listClasses);
		}
	}

	private static void findClassesInPathsDir(String strPathElement, File dir, Class<?>[] parentClasses, Set<String> listClasses) throws IOException {
		logger.debug("Buscando clases en el directorio: " + strPathElement);
		String[] list = dir.list();
		for (int i = 0; i < list.length; i++) {
			File file = new File(dir, list[i]);
			if (file.isDirectory()) {
				findClassesInPathsDir(strPathElement, file, parentClasses, listClasses);
			} else if (list[i].endsWith(DOT_CLASS) && file.exists() && (file.length() != 0)) {
				final String path = file.getPath();
				String _class = path.substring(strPathElement.length() + 1, path.lastIndexOf('.')).replace(File.separator.charAt(0), '.');

				boolean isChild = isChildOf(parentClasses, _class, contextClassLoader);
				if (isChild) {
					listClasses.add(_class);
				}
			}
		}
	}

	private static void findClassesInJarFile(File file, Class<?>[] parentClasses, Set<String> listClasses) {
		logger.debug("Buscando clases en el directorio: " + file.getPath());
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				String strEntry = entries.nextElement().toString();
				if (strEntry.endsWith(DOT_CLASS)) {
					String _class = fixClassName(strEntry);

					boolean isChild = isChildOf(parentClasses, _class, contextClassLoader);
					if (isChild) {
						listClasses.add(fixClassName(strEntry));
					}
				}
			}
		} catch (IOException e) {
			logger.warn("Can not open the jar " + file.getPath(), e);
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Converts a class file from the text stored in a Jar file to a version
	 * that can be used in Class.forName().
	 * 
	 * @param strClassName
	 *            the class name from a Jar file
	 * @return String the Java-style dotted version of the name
	 */
	private static String fixClassName(String strClassName) {
		strClassName = strClassName.replace('\\', '.');
		strClassName = strClassName.replace('/', '.');
		strClassName = strClassName.substring(0, strClassName.length() - DOT_CLASS.length());
		return strClassName;
	}

	/**
	 *
	 * @param parentClasses
	 *            list of classes to check for
	 * @param strClassName
	 *            name of class to be checked
	 * @param innerClasses
	 *            should we allow inner classes?
	 * @param contextClassLoader
	 *            the classloader to use
	 * @return true if the class is a non-abstract, non-interface instance of at
	 *         least one of the parent classes
	 */
	private static boolean isChildOf(Class<?>[] parentClasses, String strClassName, ClassLoader contextClassLoader) {
		try {
			Class<?> c = Class.forName(strClassName, false, contextClassLoader);

			if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
				for (int i = 0; i < parentClasses.length; i++) {
					if (parentClasses[i].isAssignableFrom(c)) {
						return true;
					}
				}
			}
		} catch (UnsupportedClassVersionError ignored) {
			logger.warn(ignored);
		} catch (NoClassDefFoundError ignored) {
			logger.warn(ignored);
		} catch (ClassNotFoundException ignored) {
			logger.warn(ignored);
		} catch (IllegalAccessError ignored) {
			logger.warn(ignored);
		}
		return false;
	}
}
