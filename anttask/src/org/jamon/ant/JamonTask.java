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
 * The Original Code is Jamon code, released February, 2003.
 *
 * The Initial Developer of the Original Code is Jay Sachs.  Portions
 * created by Jay Sachs are Copyright (C) 2003 Jay Sachs.  All Rights
 * Reserved.
 *
 * Contributor(s): Luis O'Shea, Ian Robertson
 */

package org.jamon.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;

import org.apache.tools.ant.types.Path;

import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;

import org.apache.tools.ant.taskdefs.MatchingTask;

import org.jamon.TemplateProcessor;
import org.jamon.JamonParseException;

/**
 * Ant task to convert Jamon templates into Java.
 **/

public class JamonTask
    extends MatchingTask
{

    public void setDestdir(File p_destDir)
    {
        m_destDir = p_destDir;
    }

    public void setSrcdir(File p_srcDir)
    {
        m_srcDir = p_srcDir;
    }

    public void execute()
        throws BuildException
    {
        // Copied from org.apache.tools.ant.taskdefs.Javac below

        // first off, make sure that we've got a srcdir

        if (m_srcDir == null)
        {
            throw new BuildException("srcdir attribute must be set!",
                                     location);
        }
        if (m_destDir == null)
        {
            throw new BuildException("destdir attribute must be set!",
                                     location);
        }

        if (! m_srcDir.exists() && ! m_srcDir.isDirectory())
        {
            throw new BuildException("source directory \"" +
                                     m_srcDir +
                                     "\" does not exist or is not a directory",
                                     location);
        }

        m_destDir.mkdirs();
        if (! m_destDir.exists() || ! m_destDir.isDirectory())
        {
            throw new BuildException("destination directory \"" +
                                     m_destDir +
                                     "\" does not exist or is not a directory",
                                     location);
        }

        if (!m_srcDir.exists())
        {
            throw new BuildException("srcdir \""
                                     + m_srcDir
                                     + "\" does not exist!", location);
        }

        GlobPatternMapper m = new GlobPatternMapper();
        m.setFrom("*");
        m.setTo("*.java");
        SourceFileScanner sfs = new SourceFileScanner(this);
        File[] files = sfs.restrictAsFiles
            (getDirectoryScanner(m_srcDir).getIncludedFiles(),
             m_srcDir,
             m_destDir,
             m);

        TemplateProcessor processor =
            new TemplateProcessor(m_destDir, m_srcDir);

        try
        {
            for (int i = 0; i < files.length; i++)
            {
                processor.generateSource(relativize(files[i]));
            }
        }
        catch (JamonParseException e)
        {
            throw new BuildException(e.getDescription(),
                                     new Location(e.getFileName(),
                                                  e.getLine(),
                                                  e.getColumn()));
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }

    private String relativize(File p_file)
    {
        if (!p_file.isAbsolute())
        {
            throw new IllegalArgumentException("Paths must be all absolute");
        }
        String filePath = p_file.getPath();
        String basePath = m_srcDir.getAbsoluteFile().toString(); // FIXME !?

        if (filePath.startsWith(basePath))
        {
            return filePath.substring(basePath.length() + 1);
        }
        else
        {
            throw new IllegalArgumentException(p_file
                                               + " is not based at "
                                               + basePath);
        }
    }

    private File m_destDir;
    private File m_srcDir;
}
