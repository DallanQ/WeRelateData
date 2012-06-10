package org.folg.werelatedata.examples;

import org.folg.werelatedata.editor.PageEditor;
import org.folg.werelatedata.parser.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
public class AddFhlcIdToPlaces {
   private static Logger logger = Logger.getLogger("org.folg.werelatedata.examples");
   private static final Pattern ALTERNATE_NAMES_BOX = Pattern.compile("<textarea[^>]*?name=\"alternateNames\"[^>]*>(.*?)</textarea>", Pattern.DOTALL);
   private static final Pattern SEE_ALSO_BOX = Pattern.compile("<textarea[^>]*?name=\"seeAlso\"[^>]*>(.*?)</textarea>", Pattern.DOTALL);
   private static final Pattern ALSO_LOCATED_IN_BOX = Pattern.compile("<textarea[^>]*?name=\"alsoLocatedIn\"[^>]*>(.*?)</textarea>", Pattern.DOTALL);
   private static final Pattern TYPE_BOX = Pattern.compile("<input[^>]*?name=\"type\"[^>]*?value=\"(.*?)\"[^/]*/>",Pattern.DOTALL);
   private static final Pattern LATITUDE_BOX = Pattern.compile("<input[^>]*?name=\"latitude\"[^>]*?value=\"(.*?)\"[^/]*/>",Pattern.DOTALL);
   private static final Pattern LONGITUDE_BOX = Pattern.compile("<input[^>]*?name=\"longitude\"[^>]*?value=\"(.*?)\"[^/]*/>",Pattern.DOTALL);
   private static final Pattern FROMYEAR_BOX = Pattern.compile("<input[^>]*?name=\"fromYear\"[^>]*?value=\"(.*?)\"[^/]*/>",Pattern.DOTALL);
   private static final Pattern TOYEAR_BOX = Pattern.compile("<input[^>]*?name=\"toYear\"[^>]*?value=\"(.*?)\"[^/]*/>",Pattern.DOTALL);

   private BufferedReader readIn;

   public void openFile(String file) throws IOException {
      readIn = new BufferedReader(new FileReader(file));
   }

   public void close() throws IOException {
      readIn.close();
   }

   public void editPages(String host, String username, String password)throws IOException{
      PageEditor edit = new PageEditor(host,username, password);
      while(readIn.ready()){
         String currentLine = readIn.readLine();
         String title = currentLine.substring(0,currentLine.indexOf('|'));
         String fhlcId = currentLine.substring(currentLine.indexOf('|') +1);
         edit.doGet("Place:" + title,true);//will be true later now just for testing
         String text = edit.readVariable(PageEditor.TEXTBOX1_PATTERN);
         if (Util.isEmpty(text)) {
            logger.warning("PLACE NOT FOUND: " + title);
         }
         else if(text.contains("{{source-fhlc|" + fhlcId + "}}") || text.contains("{{Source-fhlc|" + fhlcId + "}}")) {
            logger.warning("FHLC_ID EXISTS: " + title);
         }
         else {
            text = "{{source-fhlc|" +fhlcId +"}}" + text;
            System.out.println("title="+title);
            edit.setPostVariable("alternateNames", edit.readVariable(ALTERNATE_NAMES_BOX));
            edit.setPostVariable("seeAlso", edit.readVariable(SEE_ALSO_BOX));
            edit.setPostVariable("alsoLocatedIn", edit.readVariable(ALSO_LOCATED_IN_BOX));
            edit.setPostVariable("toYear", edit.readVariable(TOYEAR_BOX));
            edit.setPostVariable("fromYear", edit.readVariable(FROMYEAR_BOX));
            edit.setPostVariable("longitude", edit.readVariable(LONGITUDE_BOX));
            edit.setPostVariable("latitude", edit.readVariable(LATITUDE_BOX));
            edit.setPostVariable("type",edit.readVariable(TYPE_BOX));
            edit.setPostVariable("wpSummary","automated edit to add fhlc link");
            edit.setPostVariable("wpTextbox1", text);
            edit.doPost();
         }
      }
   }

   // add fhlcId's to places in file
   // file format is: each line contains: placeTitle|fhlcId
   public static void main(String[] args) throws IOException
   {
      if (args.length < 4) {
         System.out.println("Usage: <placeFile to process> <host> <username> <password>");
      }
      else {
         AddFhlcIdToPlaces self = new AddFhlcIdToPlaces();
         self.openFile(args[0]);
         self.editPages(args[1], args[2], args[3]);
         self.close();
      }
   }
}
