package org.jamon.parser;

import java.io.IOException;

import org.jamon.ParserError;
import org.jamon.ParserErrors;
import org.jamon.node.AbstractBodyNode;
import org.jamon.node.ElseIfNode;
import org.jamon.node.ElseNode;
import org.jamon.node.Location;

public class IfParser extends AbstractFlowControlBlockParser
{
    public static final String ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG =
        "encountered multiple <%else> tags for one <%if ...%> tag";

    public IfParser(AbstractBodyNode p_node,
                    PositionalPushbackReader p_reader,
                    ParserErrors p_errors) throws IOException
    {
        super(p_node, p_reader, p_errors);
    }

    @Override protected void handleElseTag(Location p_tagLocation)
        throws IOException
    {
        if (processingElseNode())
        {
            addError(
                p_tagLocation, ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG);
        }
        else
        {
            if (checkForTagClosure(p_tagLocation))
            {
                m_continuation = new IfParser(
                    new ElseNode(p_tagLocation), m_reader, m_errors);
            }
            doneParsing();
        }
    }

    @Override protected void handleElseIfTag(Location p_tagLocation)
        throws IOException
    {
        if (processingElseNode())
        {
            addError(
                p_tagLocation, ENCOUNTERED_MULTIPLE_ELSE_TAGS_FOR_ONE_IF_TAG);
        }
        else
        {
            try
            {
                m_continuation = new IfParser(
                    new ElseIfNode(
                        p_tagLocation, readCondition(p_tagLocation, "elseif")),
                    m_reader,
                    m_errors);
            }
            catch (ParserError e)
            {
                addError(e);
            }
            doneParsing();
        }
    }

    private boolean processingElseNode()
    {
        return m_root instanceof ElseNode;
    }

    public IfParser getContinuation()
    {
        return m_continuation;
    }

    private IfParser m_continuation;

    @Override protected String tagName()
    {
        return "if";
    }
}