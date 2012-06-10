package org.folg.werelatedata.examples;

import nu.xom.ParsingException;
import org.folg.werelatedata.parser.WikiParser;
import org.folg.werelatedata.parser.WikiReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/*
 * Copyright 2012 Foundation for On-Line Genealogy Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class FindNoUsernameMySources extends WikiParser {
   private PrintWriter out = null;

   public void parse(String title, String text, int pageId, int latestRevId, String username, String timestamp, String comment)
           throws IOException {
      if (title.startsWith("MySource:") && !title.contains("/")) {
         out.println("* [["+title+"]]");
      }
   }

   private void setOutput(PrintWriter out) {
      this.out = out;
   }

   // Generate a list of mysource titles without a / in the title
   // args array: 0=pages.xml 1=titles.wiki
   public static void main(String[] args) throws IOException, ParsingException {
      if (args.length != 2) {
         System.out.println("Usage: <pages file in> <titles wikitext out>");
         System.exit(1);
      }

      WikiReader wikiReader = new WikiReader();
      wikiReader.setSkipRedirects(true);
      FindNoUsernameMySources self = new FindNoUsernameMySources();
      PrintWriter out = new PrintWriter(args[1]);
      self.setOutput(out);
      wikiReader.addWikiParser(self);
      InputStream in = new FileInputStream(args[0]);
      wikiReader.read(in);
      in.close();
      out.close();
   }
}
