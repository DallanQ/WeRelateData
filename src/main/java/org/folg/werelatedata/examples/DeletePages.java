package org.folg.werelatedata.examples;

import org.folg.werelatedata.editor.PageEditor;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Logger;

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
public class DeletePages
{
   private static Logger logger = Logger.getLogger("org.folg.werelatedata.examples");

   private PageEditor editor;
   private String prefix;
   public int cnt;

   public DeletePages(String host, String username, String password, String prefix) {
      logger.info("host="+host+" username="+username+" password="+password+" prefix="+prefix);
      editor = new PageEditor(host, username, password);
      this.prefix = prefix;
      cnt = 0;
   }

   public void delete(String title, String reason) throws UnsupportedEncodingException
   {
      try {
         editor.doGet(prefix+title, false, "action=delete");
      }
      catch (RuntimeException e) {
         logger.warning("Error reading: "+title+" => "+ e);
         return;
      }

      String contents = editor.getContents();
      if (contents.contains("<h1 class=\"firstHeading\">Internal error</h1>")) {
         logger.warning("Page not found: "+title);
      }
      else {
         editor.setPostVariable("wpReason", reason);
         editor.setPostVariable("wpConfirmB", "Delete page");
         editor.doPost("delete", null);

         contents = editor.getContents();
         if (!contents.contains("has been deleted")) {
            logger.warning("Error deleting page: "+title);
         }
         else if (++cnt % 100 == 0) {
            System.out.print(".");
         }
      }
   }

   // delete titles in file
   public static void main(String[] args) throws IOException
   {
      if (args.length < 5) {
         System.out.println("Usage: <titlesFile> <host> <username> <password> <reason> [prefix]");
      }
      else {
         DeletePages dp = new DeletePages(args[1], args[2], args[3], args.length > 5 ? args[5] : "");

         BufferedReader in = new BufferedReader(new FileReader(args[0]));
         while(in.ready()){
            String line = in.readLine();
            dp.delete(line, args[4]);
         }
         in.close();
         System.out.println("Deleted "+dp.cnt);
   }
   }
}
