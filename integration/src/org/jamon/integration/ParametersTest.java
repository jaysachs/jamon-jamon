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
 * The Initial Developer of the Original Code is Ian Robertson.  Portions
 * created by Ian Robertson are Copyright (C) 2003 Ian Robertson.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.integration;

import org.jamon.JamonException;

public class ParametersTest
    extends BrokenTestBase
{
    public void testUnusedAnonDefFragment()
        throws Exception
    {
        checkForFailure
            ("UnusedAnonDefFragment",
             "Call to foo provides a fragment, but none are expected");
    }

    public void testUnusedAnonTemplateFragment()
        throws Exception
    {
        checkForFailure("UnusedAnonTemplateFragment",
                        "Call to /test/jamon/Arguments provides a fragment, but none are expected");
    }

    public void testUnusedNamedTemplateFragment()
        throws Exception
    {
        checkForFailure("UnusedNamedTemplateFragment",
                        "Call to /test/jamon/Arguments provides unused fragments content");
    }

    public void testUnusedNamedDefFragment()
        throws Exception
    {
        checkForFailure("UnusedNamedDefFragment",
                        "Call to foo provides unused fragments content");
    }

    public void testSingleFragmentCallToMultiFragmentUnit()
        throws Exception
    {
        checkForFailure("MultiFarg",
                        "Call to foo must provide multiple fragments");
    }

    public void testUnusedDefArgument()
        throws Exception
    {
        checkForFailure("UnusedDefArgument",
                        "Call to foo provides unused arguments x");
    }

    public void testUnusedTemplateArgument()
        throws Exception
    {
        checkForFailure
            ("UnusedTemplateArgument",
             "Call to /test/jamon/Arguments provides unused arguments x");
    }

    public void testMissingDefFragment()
        throws Exception
    {
        checkForFailure
            ("MissingDefFragment",
             "Call to foo is missing fragment content");
    }

    public void testMissingTemplateFragment()
        throws Exception
    {
        checkForFailure
            ("MissingTemplateFragment",
             "Call to /test/jamon/SubZ is missing fragment f");
    }

    public void testMissingRequiredArgumentForDef()
        throws Exception
    {
        checkForFailure("MissingRequiredArgument",
                        "No value supplied for required argument x");
    }

    public void testMissingRequiredArgumentForTemplate()
        throws Exception
    {
        checkForFailure("MissingTemplateRequiredArgument",
                        "No value supplied for required argument i in call to /test/jamon/Arguments");
    }

    public void testFictionalParentArgument()
        throws Exception
    {
        checkForFailure("FictionalParentArgument",
                        "/test/jamon/broken/FictionalParentArgument mistakenly thinks that /test/jamon/Parent has an arg named nosucharg");
    }

    public void testDuplicateArgument()
        throws Exception
    {
        checkForFailure("DuplicateArgument",
                        "/test/jamon/broken/DuplicateArgument has multiple arguments named opt1");
    }

    private void checkForFailure(String p_template, String p_message)
        throws Exception
    {
        try
        {
            generateSource("test/jamon/broken/" + p_template);
            fail("No exception thrown");
        }
        catch(JamonException e)
        {
            assertEquals(p_message, e.getMessage());
        }
    }
}