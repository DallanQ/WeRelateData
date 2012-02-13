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

import nu.xom.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * User: dallan
 * Date: 2/12/12
 */
public class WikiReader extends NodeFactory {
   private static Logger logger = Logger.getLogger("org.folg.werelatedata.parser");

   private Nodes EMPTY = new Nodes();
   private boolean inTitle;
   private boolean inText;
   private boolean inRevId;
   private boolean inPageId;
   private boolean inRevision;
   private boolean inUsername;
   private boolean inTimestamp;
   private boolean inComment;
   private String title;
   private String text;
   private String revId;
   private String pageId;
   private int latestRevId;
   private String latestText;
   private String username;
   private String timestamp;
   private String comment;
   private int cnt;
   private List<WikiParser> parsers;
   private boolean skipRedirects;

    public WikiReader() {
      parsers = new ArrayList<WikiParser>();
      inTitle = false;
      inText = false;
      inRevId = false;
      inPageId = false;
      inRevision = false;
      inUsername = false;
      inTimestamp = false;
      inComment = false;
      skipRedirects = true;
   }

   public void setSkipRedirects(boolean skipRedirects) {
        this.skipRedirects = skipRedirects;
   }

   public Nodes makeComment(String data) {
       return EMPTY;
   }

   public Nodes makeText(String data) {
      if (inTitle) {
         title = data;
      }
      else if (inText) {
         text = data;
      }
      else if (inRevId) {
         revId = data;
      }
      else if (inPageId) {
         pageId = data;
      }
      else if (inUsername) {
         username = data;
      }
      else if (inTimestamp) {
         timestamp = data;
      }
      else if (inComment) {
         comment = data;
      }
      return EMPTY;
   }

   public Element makeRootElement(String name, String namespace) {
       return new Element(name, namespace);
   }

   public Nodes makeAttribute(String name, String namespace,
     String value, Attribute.Type type) {
       return EMPTY;
   }

   public Nodes makeDocType(String rootElementName,
     String publicID, String systemID) {
       return EMPTY;
   }

   public Nodes makeProcessingInstruction(
     String target, String data) {
       return EMPTY;
   }

   public Element startMakingElement(String name, String namespace) {
      boolean keep = false;
      if (name.equals("page")) {
         title = "";
         latestRevId = 0;
         latestText = "";
         pageId = "";
         keep = true;
      }
      else if (name.equals("title")) {
         inTitle = true;
         keep = true;
      }
      else if (name.equals("revision")) {
         inRevision = true;
         revId = "";
         text = "";
         username = "";
         timestamp = "";
         comment = "";
         keep = true;
      }
      else if (!inRevision && name.equals("id")) {
         inPageId = true;
         keep = true;
      }
      else if (inRevision && name.equals("id") && revId.length() == 0) {  // ignore ID under page, and later id's under contributor
         inRevId = true;
         keep = true;
      }
      else if (inRevision && name.equals("text")) {
         inText = true;
         keep = true;
      }
      else if (name.equals("username")) {
         inUsername = true;
         keep = true;
      }
      else if (name.equals("timestamp")) {
         inTimestamp = true;
         keep = true;
      }
      else if (name.equals("comment")) {
         inComment = true;
         keep = true;
      }
      if (keep) {
         return super.startMakingElement(name, namespace);
      }
      return null;
   }

   public Nodes finishMakingElement(Element element) {
      if (element.getParent() instanceof Document) {
         return new Nodes(element);
      }
      String localName = element.getLocalName();
      if (localName.equals("revision")) {
         if (revId.length() > 0) {
            try {
               int idNumber = Integer.parseInt(revId);
               if (idNumber > latestRevId) {
                  latestRevId = idNumber;
                  latestText = text;
               }
               else {
                  logger.warning("IDs (" + latestRevId + " -> " + revId + ") out of sequence for title: " + title);
               }
            }
            catch (NumberFormatException e) {
               logger.warning("Invalid ID: " + revId + " for title: " + title);
            }
         }
         inRevision = false;
      }
      else if (localName.equals("page")) {
         if (++cnt % 100000 == 0) {
            System.out.print(".");
         }

         Matcher m = Util.REDIRECT_PATTERN.matcher(latestText);
         if (title.length() == 0) {
            logger.warning("empty title");
         }
         else if (skipRedirects && m.lookingAt()) {
            // logger.info("skipping redirect: " + title);
         }
         else {
            for (WikiParser parser:parsers) {
               try {
                  parser.parse(title, latestText, Integer.parseInt(pageId), latestRevId, username, timestamp, comment);
               } catch (IOException e) {
                  logger.severe("IOException: " + e);
               } catch (ParsingException e) {
                  logger.severe("Parsing exception for title: " + title + " - " + e);
               }
            }
         }
      }
      inTitle = false;
      inText = false;
      inRevId = false;
      inPageId = false;
      inUsername = false;
      inTimestamp = false;
      inComment = false;
      return EMPTY;
   }

   public void addWikiParser(WikiParser parser) {
      parsers.add(parser);
   }

   public void removeWikiParser(WikiParser parser) {
      parsers.remove(parser);
   }

   public void read(String filename) throws ParsingException, IOException {
      InputStream in = new FileInputStream(filename);
      read(in);
      in.close();
   }

   public void read(InputStream in) throws ParsingException, IOException {
      title = null;
      cnt = 0;
      System.out.print("Reading");
      Builder builder = new Builder(this);
      builder.build(in);
      System.out.println();
   }
}
