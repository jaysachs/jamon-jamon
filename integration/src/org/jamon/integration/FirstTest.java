package org.jamon.integration;

import java.io.Writer;
import java.io.StringWriter;
import java.math.BigDecimal;

import junit.framework.TestCase;

import gnu.regexp.RE;

import org.jamon.StandardTemplateManager;
import org.jamon.StringUtils;

import foo.bar.test.jamon.TestTemplate;

public class FirstTest
    extends TestCase
{

    public void testExercise()
        throws Exception
    {
        Writer w = new StringWriter();
        StandardTemplateManager m =
            new StandardTemplateManager("templates",
                                        "build/work");
        m.setPackagePrefix("foo.bar.");
        TestTemplate.Factory f = new TestTemplate.Factory(m);
        TestTemplate t = f.getInstance(w);
        t.setX(57);
        t.render(new BigDecimal("34.5324"));
        w.flush();

        RE re = new RE(".*An external template with a parameterized fragment parameter \\(farg\\)" +
                       "\\s*" +
                       "i is 3 and s is yes." +
                       "\\s*" +
                       "i is 7 and s is no.*",
                       RE.REG_DOT_NEWLINE);
        assertTrue(re.isMatch(w));
    }

}