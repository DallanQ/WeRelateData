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
package org.folg.werelatedata.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import nu.xom.ParsingException;
import nu.xom.Element;
import nu.xom.Elements;

import org.folg.werelatedata.parser.Util;
import org.folg.werelatedata.parser.WikiParser;
import org.folg.werelatedata.parser.WikiReader;

/**
 * This class generates a one-line summary for every person
 * User: dallan
 * Date: 2/12/12
 */
public class People extends WikiParser {
   private PrintWriter out = null;

   public void parse(String title, String text, int pageId, int latestRevId, String username, String timestamp, String comment)
           throws IOException, ParsingException {
      if (title.startsWith("Person:")) {
         title = title.substring("Person:".length());
         String[] split = splitStructuredWikiText("person", text);
         String structuredData = split[0];
         if (!Util.isEmpty(structuredData)) {
            Element root = parseText(structuredData).getRootElement();
            Element elm;
            Elements elms;
            String given = "";
            String surname = "";
            String birthDate = "";
            String birthPlace = "";
            String deathDate = "";
            String deathPlace = "";

            // get name
            elms = root.getChildElements("name");
            if (elms.size() > 0) {
               elm = elms.get(0);
               given = Util.nullToEmpty(elm.getAttributeValue("given"));
               surname = Util.nullToEmpty(elm.getAttributeValue("surname"));
            }
            // get {birth,death}{date,place}
            elms = root.getChildElements("event_fact");
            for (int i = 0; i < elms.size(); i++) {
               elm = elms.get(i);
               if ("Birth".equals(elm.getAttributeValue("type"))) {
                  birthDate = Util.nullToEmpty(elm.getAttributeValue("date"));
                  birthPlace = Util.nullToEmpty(Util.getPreBar(elm.getAttributeValue("place")));
               }
               else if ("Death".equals(elm.getAttributeValue("type"))) {
                  deathDate = Util.nullToEmpty(elm.getAttributeValue("date"));
                  deathPlace = Util.nullToEmpty(Util.getPreBar(elm.getAttributeValue("place")));
               }
            }

            out.println(title+"|"+given+"|"+surname+"|"+birthDate+"|"+birthPlace+"|"+deathDate+"|"+deathPlace);
         }
      }
   }

   private void setOutput(PrintWriter out) {
      this.out = out;
   }

   // Generate various lists of places
   // args array: 0=pages.xml 1=people.csv
   public static void main(String[] args)
           throws IOException, ParsingException
   {
      WikiReader wikiReader = new WikiReader();
      wikiReader.setSkipRedirects(true);
      People self = new People();
      PrintWriter out = new PrintWriter(args[1]);
      self.setOutput(out);
      wikiReader.addWikiParser(self);
      InputStream in = new FileInputStream(args[0]);
      wikiReader.read(in);
      in.close();
      out.close();
   }
}
