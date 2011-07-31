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
 * The Initial Developer of the Original Code is Luis O'Shea.  Portions
 * created by Luis O'Shea are Copyright (C) 2003 Luis O'Shea.  All Rights
 * Reserved.
 *
 * Contributor(s):
 */

package org.jamon.escaping;

import java.io.Writer;
import java.io.IOException;

public class XmlEscaping extends AbstractCharacterEscaping {

  XmlEscaping() {} // package scope constructor

  @Override
  protected void write(char character, Writer writer) throws IOException {
    switch (character) {
      case '<': writer.write("&lt;"); break;
      case '>': writer.write("&gt;"); break;
      case '&': writer.write("&amp;"); break;
      case '"': writer.write("&quot;"); break;
      case '\'': writer.write("&apos;"); break;
        // FIXME: numerically escape other chars outside ASCII
      default: writer.write(character);
    }
  }
}
