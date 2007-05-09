package org.jamon.parser;

import java.io.IOException;

import org.jamon.compiler.ParserErrorImpl;
import org.jamon.compiler.ParserErrorsImpl;
import org.jamon.node.AbstractArgsNode;
import org.jamon.node.ArgNameNode;
import org.jamon.node.ArgTypeNode;
import org.jamon.node.FragmentArgsNode;

public class FragmentArgsParser extends AbstractArgsParser
{
    public static final String EXPECTING_GREATER = "Expecting '>'";
    public static final String NEED_SEMI= "Expecting a ';'";
    public static final String FRAGMENT_ARGUMENT_HAS_NO_NAME =
        "Fragment argument has no name";

    /**
     * @param p_reader
     * @param p_errors
     * @param p_tagLocation
     * @throws IOException
     * @throws ParserErrorImpl
     */
    public FragmentArgsParser(PositionalPushbackReader p_reader,
                              ParserErrorsImpl p_errors,
                              org.jamon.api.Location p_tagLocation)
        throws IOException, ParserErrorImpl
    {
        super(p_reader, p_errors, p_tagLocation);
    }

    public FragmentArgsNode getFragmentArgsNode()
    {
        return m_fragmentArgsNode;
    }

    @Override protected boolean handleDefaultValue(
        AbstractArgsNode argsNode, ArgTypeNode argType, ArgNameNode argName)
    {
        //Fragment arguments cannot have default values
        return false;
    }

    @Override protected void checkArgsTagEnd() throws IOException
    {
        if (!checkToken("/%frag>"))
        {
            addError(m_reader.getLocation(), BAD_ARGS_CLOSE_TAG);
        }
    }

    @Override protected String postArgNameTokenError()
    {
        return NEED_SEMI;
    }

    @Override protected AbstractArgsNode makeArgsNode(org.jamon.api.Location p_tagLocation)
    {
        return m_fragmentArgsNode =
            new FragmentArgsNode(p_tagLocation, m_fragmentName);
    }

    @Override protected boolean finishOpenTag(org.jamon.api.Location p_tagLocation)
        throws IOException
    {
        if(!soakWhitespace())
        {
            m_fragmentName = "";
            addError(p_tagLocation, FRAGMENT_ARGUMENT_HAS_NO_NAME);
        }
        else
        {
            try
            {
                m_fragmentName = readIdentifierOrThrow();
            }
            catch (NotAnIdentifierException e)
            {
                addError(p_tagLocation, FRAGMENT_ARGUMENT_HAS_NO_NAME);
            }
        }
        soakWhitespace();
        if (readChar('/'))
        {
            if (!readChar('>'))
            {
                addError(m_reader.getCurrentNodeLocation(), EXPECTING_GREATER);
            }
            else
            {
                m_fragmentArgsNode =
                    new FragmentArgsNode(p_tagLocation, m_fragmentName);
                soakWhitespace();
            }
            return false;
        }
        return checkForTagClosure(p_tagLocation);
    }

    private String m_fragmentName;
    private FragmentArgsNode m_fragmentArgsNode;
}