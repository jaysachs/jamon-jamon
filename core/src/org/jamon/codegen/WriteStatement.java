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
 * Contributor(s): Luis O'Shea
 */

package org.jamon.codegen;

public class WriteStatement
    implements Statement
{
    WriteStatement(String p_expr, EscapingDirective p_escapingDirective)
    {
        m_expr = p_expr;
        m_escapingDirective = p_escapingDirective;
    }

    public void generateSource(IndentingWriter p_writer,
                               TemplateResolver p_resolver,
                               TemplateDescriber p_describer,
                               ImplAnalyzer p_analyzer)
    {
        p_writer.print("this.writeEscaped(this.valueOf(");
        p_writer.print(m_expr);
        p_writer.print(")");
        if (!m_escapingDirective.isDefault())
        {
            p_writer.print(", ");
            p_writer.print(m_escapingDirective.toJava());
        }
        p_writer.println(");");
    }

    private final String m_expr;
    private final EscapingDirective m_escapingDirective;
}