/*
 * Copyright 2012 Foundation for On-Line Genealogy, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.folg.werelatedata.parser;

import nu.xom.Builder;
import nu.xom.ParsingException;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

/**
 * User: dallan
 * Date: 2/12/12
 */
public abstract class WikiParser {
   protected static final Logger logger = Logger.getLogger("org.folg.werelatedata.parser");

   public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

   private Builder builder;

	public abstract void parse(String title, String text, int pageId, int latestRevId, String username, String timestamp, String comment) throws IOException, ParsingException;

   public WikiParser() {
      builder = new Builder();
   }

   protected nu.xom.Document parseText(String text) throws ParsingException, IOException
	{
      return builder.build(new StringReader(XML_HEADER + text));
   }

   /**
    * Returns the structured text in position 0 of the array; wiki text in position 1
    * @param tagName the name of the xml tag to search for
    * @param text the text to search in
    */
   public static String[] splitStructuredWikiText(String tagName, String text) {
      String[] split = new String[2];
      String endTag = "</" + tagName + ">";
      int pos = text.indexOf(endTag);
      if (pos >= 0) {
         pos += endTag.length();
         // skip over \n if present
         split[0] = text.substring(0, pos);
         split[1] = text.substring(pos);
      }
      else {
         split[0] = null;
         split[1] = text;
      }
      return split;
   }
}
