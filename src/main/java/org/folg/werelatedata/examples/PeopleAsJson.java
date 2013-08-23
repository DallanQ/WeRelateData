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
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import nu.xom.ParsingException;
import nu.xom.Element;
import nu.xom.Elements;

import org.folg.werelatedata.parser.Util;
import org.folg.werelatedata.parser.WikiParser;
import org.folg.werelatedata.parser.WikiReader;

/**
 * This class generates a json summary for every person
 * It's in a strange format -- one json object per line, with rather odd field names
 * User: dallan
 */
public class PeopleAsJson extends WikiParser {
   private static class Person {
      String person_id = "";
      String first_names = "";
      String last_names = "";
      String gender = "";
      String birth_date_str = "";
      String birth_place = "";
      String marriage_date_str = "";
      String marriage_place = "";
      String death_date_str = "";
      String death_place = "";
      String father_first_names = "";
      String father_last_names = "";
      String mother_first_names = "";
      String mother_last_names = "";
      String spouse_first_names = "";
      String spouse_last_names = "";
      String child_of_family_title = "";
      String spouse_of_family_title = "";
   }

   private static class Family {
      String husbandGiven = "";
      String husbandSurname = "";
      String wifeGiven = "";
      String wifeSurname = "";
      String marriageDate = "";
      String marriagePlace = "";
   }

   public Map<String,Person> people = new HashMap<String, Person>();
   private Map<String,Family> families = new HashMap<String, Family>();

   public void parse(String title, String text, int pageId, int latestRevId, String username, String timestamp, String comment)
           throws IOException, ParsingException {
      if (title.startsWith("Family")) {
         title = title.substring("Family:".length());
         String[] split = splitStructuredWikiText("family", text);
         String structuredData = split[0];
         if (!Util.isEmpty(structuredData)) {
            Element root = parseText(structuredData).getRootElement();
            Element elm;
            Elements elms;
            Family f = new Family();

            // get {husband,wife}{given,surname}
            elms = root.getChildElements("husband");
            if (elms.size() > 0) {
               elm = elms.get(0);
               f.husbandGiven = Util.nullToEmpty(elm.getAttributeValue("given"));
               f.husbandSurname = Util.nullToEmpty(elm.getAttributeValue("surname"));
            }
            elms = root.getChildElements("wife");
            if (elms.size() > 0) {
               elm = elms.get(0);
               f.wifeGiven = Util.nullToEmpty(elm.getAttributeValue("given"));
               f.wifeSurname = Util.nullToEmpty(elm.getAttributeValue("surname"));
            }

            // get marriage{date,place}
            elms = root.getChildElements("event_fact");
            for (int i = 0; i < elms.size(); i++) {
               elm = elms.get(i);
               if ("Marriage".equals(elm.getAttributeValue("type"))) {
                  f.marriageDate = Util.nullToEmpty(elm.getAttributeValue("date"));
                  f.marriagePlace = Util.nullToEmpty(Util.getPreBar(elm.getAttributeValue("place")));
               }
            }

            families.put(title, f);
         }
      }
      else if (title.startsWith("Person:")) {
         title = title.substring("Person:".length());
         String[] split = splitStructuredWikiText("person", text);
         String structuredData = split[0];
         if (!Util.isEmpty(structuredData)) {
            Element root = parseText(structuredData).getRootElement();
            Element elm;
            Elements elms;
            Person p = new Person();

            p.person_id = title;

            // get name
            elms = root.getChildElements("name");
            if (elms.size() > 0) {
               elm = elms.get(0);
               p.first_names = Util.nullToEmpty(elm.getAttributeValue("given"));
               p.last_names = Util.nullToEmpty(elm.getAttributeValue("surname"));
            }

            // get gender
            elms = root.getChildElements("gender");
            if (elms.size() > 0) {
               elm = elms.get(0);
               p.gender = Util.nullToEmpty(elm.getValue());
            }

            // get {birth,death}{date,place}
            elms = root.getChildElements("event_fact");
            for (int i = 0; i < elms.size(); i++) {
               elm = elms.get(i);
               if ("Birth".equals(elm.getAttributeValue("type"))) {
                  p.birth_date_str = Util.nullToEmpty(elm.getAttributeValue("date"));
                  p.birth_place = Util.nullToEmpty(Util.getPreBar(elm.getAttributeValue("place")));
               }
               else if ("Death".equals(elm.getAttributeValue("type"))) {
                  p.death_date_str = Util.nullToEmpty(elm.getAttributeValue("date"));
                  p.death_place = Util.nullToEmpty(Util.getPreBar(elm.getAttributeValue("place")));
               }
            }

            // get first {child_of,spouse_of}_family
            elms = root.getChildElements("child_of_family");
            if (elms.size() > 0) {
               elm = elms.get(0);
               p.child_of_family_title = Util.nullToEmpty(elm.getAttributeValue("title"));
            }
            elms = root.getChildElements("spouse_of_family");
            if (elms.size() > 0) {
               elm = elms.get(0);
               p.spouse_of_family_title = Util.nullToEmpty(elm.getAttributeValue("title"));
            }

            people.put(title, p);
         }
      }
   }

   // Generate a json file containing all people in WeRelate (see comment at top of file)
   // args array: 0=pages.xml 1=people.json
   public static void main(String[] args)
           throws IOException, ParsingException
   {
      WikiReader wikiReader = new WikiReader();
      wikiReader.setSkipRedirects(true);
      PeopleAsJson self = new PeopleAsJson();
      wikiReader.addWikiParser(self);
      InputStream in = new FileInputStream(args[0]);
      wikiReader.read(in);
      in.close();

      Gson gson = new Gson();
      PrintWriter out = new PrintWriter(args[1]);
      for (Person p : self.people.values()) {
         if (p.child_of_family_title.length() > 0) {
            Family f = self.families.get(p.child_of_family_title);
            if (f != null) {
               p.father_first_names = f.husbandGiven;
               p.father_last_names = f.husbandSurname;
               p.mother_first_names = f.wifeGiven;
               p.mother_last_names = f.wifeSurname;
            }
         }
         if (p.spouse_of_family_title.length() > 0) {
            Family f = self.families.get(p.spouse_of_family_title);
            if (f != null) {
               if ("M".equals(p.gender)) {
                  p.spouse_first_names = f.wifeGiven;
                  p.spouse_last_names = f.wifeSurname;
               }
               else if ("F".equals(p.gender)) {
                  p.spouse_first_names = f.husbandGiven;
                  p.spouse_last_names = f.husbandSurname;
               }
               p.marriage_date_str = f.marriageDate;
               p.marriage_place = f.marriagePlace;
            }
         }
         out.println(gson.toJson(p).replace("\n", ""));
      }
      out.close();
   }
}
