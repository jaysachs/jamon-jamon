/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Jamon code, released October, 2002.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2002 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import org.jamon.Invoker;
import org.jamon.StandardTemplateManager;
import org.jamon.JamonParseException;

/**
 * Ant task to reflectively invoke templates.
 **/

public class InvokerTask
    extends Task
{
    public InvokerTask()
    {
        m_templateManager = new StandardTemplateManager();
    }

    public void execute()
        throws BuildException
    {
        try
        {
            Writer writer;
            if (m_output == null)
            {
                writer = new OutputStreamWriter(System.out);
            }
            else
            {
                File parent = m_output.getAbsoluteFile().getParentFile();
                if (parent != null)
                {
                    // FIXME: should we check for failure here
                    //   or let it fall through?
                    parent.mkdirs();
                }
                writer = new FileWriter(m_output);
            }
            new Invoker(m_templateManager,m_path)
                .render(m_output == null
                        ? new OutputStreamWriter(System.out)
                        : new FileWriter(m_output),
                        m_args);
        }
        catch (Invoker.InvalidTemplateException e)
        {
            e.printStackTrace(System.err);
        }
        catch (JamonParseException e)
        {
            throw new BuildException(e.getDescription(),
                                     new Location(e.getFileName(),
                                                  e.getLine(),
                                                  e.getColumn()));
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }

    public void setClasspath(Path p_classpath)
        throws IOException
    {
        String[] paths = p_classpath.list();
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < urls.length; ++i)
        {
            urls[i] = new URL("file",null, paths[i]);
        }
        m_templateManager
            .setClassLoader(new URLClassLoader(urls,
                                               getClass().getClassLoader()));
        m_templateManager.setClasspath(p_classpath.toString());
    }

    public void setWorkDir(File p_workDir)
    {
        m_templateManager.setWorkDir(p_workDir.getAbsolutePath());
    }

    public void setSourceDir(File p_sourceDir)
    {
        m_templateManager.setSourceDir(p_sourceDir.getAbsolutePath());
    }

    public void setOutput(File p_output)
        throws IOException
    {
        m_output = p_output;
    }

    public void setTemplate(String p_path)
    {
        m_path = p_path;
    }

    private final StandardTemplateManager m_templateManager;
    private String m_path;
    private HashMap m_args = new HashMap();
    private File m_output;

    public void addConfiguredArg(Arg p_arg)
    {
        m_args.put(p_arg.m_name, p_arg.m_value);
    }

    public static class Arg
    {
        public void setName(String p_name)
        {
            m_name = p_name;
        }
        public void setValue(String p_value)
        {
            m_value = p_value;
        }
        private String m_name;
        private String m_value;
    }
}