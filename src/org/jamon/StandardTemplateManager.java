package org.modusponens.jtt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.HashMap;
import org.modusponens.jtt.parser.Parser;
import org.modusponens.jtt.parser.ParserException;
import org.modusponens.jtt.lexer.Lexer;
import org.modusponens.jtt.lexer.LexerException;


public class StandardTemplateManager
    implements TemplateManager
{
    public StandardTemplateManager(ClassLoader p_parentLoader)
        throws IOException
    {
        m_loader = new WorkDirLoader(p_parentLoader);
    }

    public StandardTemplateManager()
        throws IOException
    {
        this(ClassLoader.getSystemClassLoader());
    }

    public Template getInstance(String p_path, Writer p_writer)
        throws JttException
    {
        try
        {
            return (Template)
                getImplementationClass(p_path)
                    .getConstructor(new Class [] { Writer.class,
                                                   TemplateManager.class })
                    .newInstance(new Object [] { p_writer, this });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JttException(e);
        }
    }

    public void setWorkDir(String p_workDir)
    {
        m_workDir = p_workDir;
        m_javaCompiler = null;
    }

    public void setTemplateSourceDir(String p_dir)
    {
        m_templateSourceDir = p_dir;
    }

    public void setJavaCompiler(String p_javac)
    {
        m_javac = p_javac;
        m_javaCompiler = null;
    }

    public void setJavaCompilerNeedsRtJar(boolean p_includeRtJar)
    {
        m_includeRtJar = p_includeRtJar;
        m_javaCompiler = null;
    }

    public void setClasspath(String p_classpath)
    {
        m_classpath = p_classpath;
        m_javaCompiler = null;
    }

    public void setPackagePrefix(String p_packagePrefix)
    {
        m_packagePrefix = p_packagePrefix;
        m_javaCompiler = null;
    }

    private String getJavaFileName(String p_path)
    {
        return m_workDir + p_path + "Impl.java";
    }

    private String getJavaClassFileName(String p_path)
    {
        return m_workDir + p_path + "Impl.class";
    }

    private String getTemplateFileName(String p_path)
    {
        return m_templateSourceDir + p_path;
    }

    private String getClassName(String p_path)
    {
        return PathUtils.pathToClassName(p_path) + "Impl";
    }

    private class WorkDirLoader
        extends ClassLoader
    {
        WorkDirLoader(ClassLoader p_parent)
        {
            super(p_parent);
        }

        private String getFileNameForClass(String p_name)
        {
            return m_workDir
                + PathUtils.classNameToPath(p_name.replace('.','/'))
                + ".class";
        }

        protected Class loadClass(String p_name, boolean p_resolve)
            throws ClassNotFoundException
        {
            System.err.println("load class " + p_name);
            File cf = new File(getFileNameForClass(p_name));
            if (!cf.exists())
            {
                System.err.println("to super " + p_name);
                return super.loadClass(p_name, p_resolve);
            }

            Long last = (Long) m_timestampMap.get(p_name);
            if (last != null && last.longValue() >= cf.lastModified())
            {
                System.err.println("up to date in cache " + p_name);
                return (Class) m_classMap.get(p_name);
            }
            if (last != null || m_loader == null)
            {
                System.err.println("creating new class loader " + p_name);
                try
                {
                    m_loader = new URLClassLoader
                        (new URL [] { new URL("file:" + m_workDir + "/") },
                         getParent());
                }
                catch (IOException e)
                {
                    throw new JttClassNotFoundException(e);
                }
            }
            System.err.println("inner load " + p_name);
            Class c = m_loader.loadClass(p_name);
            if (p_resolve)
            {
                resolveClass(c);
            }
            m_classMap.put(p_name,c);
            m_timestampMap.put(p_name, new Long(cf.lastModified()));
            return c;
        }

        private ClassLoader m_loader;

        private final Map m_classMap = new HashMap();
        private final Map m_timestampMap = new HashMap();
    }

    private WorkDirLoader getLoader()
    {
        return m_loader;
    }

    private Class loadAndResolveClass(String p_path)
        throws ClassNotFoundException
    {
        // FIXME: need to check that it still implements the interface
        return getLoader().loadClass(getClassName(p_path));
    }

    private String getClassPath()
    {
        String cp = m_classpath != null ? (m_classpath + PS) : "";
        cp = m_workDir + PS + cp + System.getProperty("java.class.path");
        if (m_includeRtJar)
        {
            cp += PS + System.getProperty("java.home") + FS + "lib" + FS +"rt.jar";
        }
        return cp;
    }

    private JavaCompiler getJavaCompiler()
    {
        if (m_javaCompiler == null)
        {
            m_javaCompiler = new JavaCompiler(m_javac, getClassPath());
        }
        return m_javaCompiler;
    }

    private void createJavaFile(String p_path)
        throws IOException,
               ParserException,
               LexerException
    {
        String packageName, className;
        int i = p_path.lastIndexOf(FS);
        if (i >= 1)
        {
            new File(m_workDir + p_path.substring(0,i)).mkdirs();
            packageName = PathUtils.pathToClassName(p_path.substring(1,i));
            className = p_path.substring(i+1);
        }
        else if (i == 0)
        {
            packageName = "";
            className = p_path.substring(1);
        }
        else
        {
            packageName = "";
            className = p_path;
        }

        File javaFile = new File(getJavaFileName(p_path));
        FileWriter writer = new FileWriter(javaFile);

        ImplGenerator g2 = new ImplGenerator(writer,
                                             m_packagePrefix,
                                             packageName,
                                             className);

        new Parser(new Lexer(new PushbackReader
                             (new FileReader(getTemplateFileName(p_path)),
                              1024)))
            .parse()
            .apply(g2);

        try
        {
            g2.generateClassSource();
            writer.close();
        }
        catch (IOException e)
        {
            writer.close();
            javaFile.delete();
            throw e;
        }
    }

    private boolean isJavaFileUpToDate(String p_path)
        throws IOException
    {
        File jf = new File(getJavaFileName(p_path));
        if (jf.exists())
        {
            File tf = new File(getTemplateFileName(p_path));
            if (! tf.exists() )
            {
                throw new JttException("Template file "
                                       + p_path
                                       + " missing?!");
            }
            return tf.lastModified() < jf.lastModified();
        }
        else
        {
            return false;
        }
    }

    private boolean isClassFileUpToDate(String p_path)
        throws IOException
    {
        File jf = new File(getJavaFileName(p_path));
        if (!jf.exists())
        {
            throw new IOException("java file disappeared!?");
        }
        File cf = new File(getJavaClassFileName(p_path));
        return jf.lastModified() < cf.lastModified();
    }

    private Class getImplementationClass(String p_path)
        throws IOException,
               ParserException,
               LexerException,
               ClassNotFoundException
    {
        if (! isJavaFileUpToDate(p_path))
        {
            createJavaFile(p_path);
        }

        if (! isClassFileUpToDate(p_path) )
        {
            getJavaCompiler().compile(getJavaFileName(p_path));
        }

        return loadAndResolveClass(p_path);
    }

    private static final String PS = System.getProperty("path.separator");
    private static final String FS = System.getProperty("file.separator");

    private String m_templateSourceDir = "testdata";
    private String m_workDir = "work";
    private String m_javac =
        System.getProperty("java.home") + FS + ".." + FS + "bin" + FS +"javac";
    private boolean m_includeRtJar = false;
    private String m_classpath = "";
    private ClassLoader m_classLoader = ClassLoader.getSystemClassLoader();
    private String m_packagePrefix = "";
    private JavaCompiler m_javaCompiler;
    private final WorkDirLoader m_loader;
}
