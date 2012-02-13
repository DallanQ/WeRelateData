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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dallan
 * Date: 2/12/12
 */
public class Util {
   public static final Pattern REDIRECT_PATTERN = Pattern.compile("\\s*#redirect:?\\s*\\[\\[([^\\|\\]]+)[^\\]]*\\]\\]", Pattern.CASE_INSENSITIVE);
   /** max wiki title length */
   public static final int MAX_TITLE_LEN = 150;

   public static String cleanRedirTarget(String target) {
      int pos = target.indexOf('|');
      if (pos >= 0) {
         target = target.substring(0, pos);
      }
      return target.replace('_',' ').trim();
   }

   /**
    * Returns whether the specified string is null or has a zero length
    * @param s string to test
    * @return boolean
    */
   public static boolean isEmpty(String s) {
      return (s == null || s.trim().length() == 0);
   }

   /**
    * Returns true if the specified string contains only 7-bit ascii characters
    * @param in string to test
    * @return boolean
    */
   public static boolean isAscii(String in) {
      for (int i = 0; i < in.length(); i++) {
         if (in.charAt(i) > 127) {
            return false;
         }
      }
      return true;
   }

   public static String join(String glue, Collection<? extends Object> c) {
      StringBuilder buf = new StringBuilder();
      for (Object o : c) {
         if (buf.length() > 0) {
            buf.append(glue);
         }
         buf.append(o.toString());
      }
      return buf.toString();
   }

   private static final String [][] XML_CHARS = {
      {"&", "&amp;"},
      {"<", "&lt;"},
      {">", "&gt;"},
      {"\"", "&quot;"},
      {"'", "&apos;"},
   };

   public static String encodeXML(String text) {
      for (int i=0; i < XML_CHARS.length; i++) {
         text = text.replace(XML_CHARS[i][0], XML_CHARS[i][1]);
      }
      return text;
   }

   public static String unencodeXML(String text) {
      for (int i=XML_CHARS.length-1; i >= 0; i--) {
         text = text.replace(XML_CHARS[i][1], XML_CHARS[i][0]);
      }
      return text;
   }

   public static String nullToEmpty(String in) {
      if (in == null) return "";
      return in;
   }

   public static String getPostBar(String in) {
      if (isEmpty(in)) return in;
      int pos = in.indexOf('|');
      if (pos >= 0) {
         return in.substring(pos+1);
      }
      return in;
   }

   public static String getPreBar(String in) {
      if (isEmpty(in)) return in;
      int pos = in.indexOf('|');
      if (pos >= 0) {
         return in.substring(0,pos);
      }
      return in;
   }

   /**
    * Return the number of occurrences of the specified character in the specified string
    */
   public static int countOccurrences(char ch, String in) {
      int cnt = 0;
      int pos = in.indexOf(ch);
      while (pos >= 0) {
         cnt++;
         pos = in.indexOf(ch, pos+1);
      }
      return cnt;
   }

   public static void sleep(int miliseconds) {
      try
      {
         Thread.sleep(miliseconds);
      } catch (InterruptedException e)
      {
      }
   }

   public static String toMixedCase(String s) {
      StringBuilder buf = new StringBuilder();
      boolean followsSpace = true;
      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (followsSpace) {
            buf.append(s.substring(i, i+1).toUpperCase()); // javadocs recommend this function instead of Character.toUpperCase(c)
         }
         else {
            buf.append(c);
         }
         followsSpace = (c == ' ');
      }
      return buf.toString();
   }

   /**
    * Translate \ to \\ and $ to \$, in preparation for using the specified string in a regexp replacement
    * @param text
    * @return
    */
   public static String protectDollarSlash(String text) {
      return text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
   }

   private static final Pattern OPEN_CLOSE_TEMPLATE = Pattern.compile("\\{\\{|\\}\\}");

   public static List<String> getTemplates(String text) {
      List<String> templates = new ArrayList<String>();
      int openTags = 0;
      int outermostStart = 0;
      Matcher openClose = OPEN_CLOSE_TEMPLATE.matcher(text);
      while (openClose.find()) {
         if (openClose.group(0).equals("{{")) {
            if (openTags == 0) {
               outermostStart = openClose.end();
            }
            openTags++;
         }
         else {
            openTags--;
            if (openTags == 0) {
               templates.add(text.substring(outermostStart, openClose.start()).trim());
            }
            else if (openTags < 0) {
               openTags = 0; // malformed
            }
         }
      }
      return templates;
   }

   private static final Map<String, Integer> MONTHS = new HashMap<String, Integer>();
   static
   {
      MONTHS.put("january", 1);
      MONTHS.put("february", 2);
      MONTHS.put("march", 3);
      MONTHS.put("april", 4);
      MONTHS.put("may", 5);
      MONTHS.put("june", 6);
      MONTHS.put("june", 6);
      MONTHS.put("july", 7);
      MONTHS.put("august", 8);
      MONTHS.put("september", 9);
      MONTHS.put("october", 10);
      MONTHS.put("november", 11);
      MONTHS.put("december", 12);
      MONTHS.put("jan", 1);
      MONTHS.put("feb", 2);
      MONTHS.put("mar", 3);
      MONTHS.put("apr", 4);
      MONTHS.put("may", 5);
      MONTHS.put("jun", 6);
      MONTHS.put("jul", 7);
      MONTHS.put("aug", 8);
      MONTHS.put("sep", 9);
      MONTHS.put("oct", 10);
      MONTHS.put("nov", 11);
      MONTHS.put("dec", 12);
      MONTHS.put("febr", 2);
      MONTHS.put("sept", 9);
   }

   private static final Map <Integer, Integer> MONTH_DAYS = new HashMap<Integer, Integer>();
   static
   {
      MONTH_DAYS.put(1, 31);
      MONTH_DAYS.put(2, 28);
      MONTH_DAYS.put(3, 31);
      MONTH_DAYS.put(4, 30);
      MONTH_DAYS.put(5, 31);
      MONTH_DAYS.put(6, 30);
      MONTH_DAYS.put(7, 31);
      MONTH_DAYS.put(8, 31);
      MONTH_DAYS.put(9, 30);
      MONTH_DAYS.put(10, 31);
      MONTH_DAYS.put(11, 30);
      MONTH_DAYS.put(12, 31);
   }

   // include non-ascii characters as alphanumeric
   private static Pattern pAlphaNumRegExp =  Pattern.compile("\\d+|[^0-9\\s`~!@#$%^&*()_+\\-={}|:'<>?;,/\"\\[\\]\\.\\\\]+");
   private static boolean isYear(int y) {
      return y >= 100 && y <= 2200;
   }
   private static int getAlphaMonth(String mon) {
     mon = mon.toLowerCase();
     if (MONTHS.get(mon) != null) {
             return MONTHS.get(mon);
     }
     return 0;
   }
   private static boolean isDay(int d) {
      return d >= 1 && d <= 31;
   }
   private static boolean isNumMonth(int m) {
      return m >= 1 && m <= 12;
   }

   private static final boolean isNumeric(final String s) {
     final char[] numbers = s.toCharArray();
     for (int x = 0; x < numbers.length; x++) {
       final char c = numbers[x];
       if ((c >= '0') && (c <= '9')) continue;
       return false; // invalid
     }
     return true; // valid
   }

   private static int parseInt(String field)
   {
      if (isNumeric(field))
      {
         try {
            return Integer.parseInt(field);
         }
         catch (NumberFormatException e) {
            // ignore
         }
      }
      return 0;
   }

   public static String getDateSortKey(String date) {
      String result = "";
      if (!Util.isEmpty(date))
      {
         int year = 0;
         int month = 0;
         int day = 0;
         Matcher mFields = pAlphaNumRegExp.matcher(date);
         List<String> fields = new ArrayList<String>();

         while (mFields.find())
         {
            fields.add(mFields.group(0));
         }

         for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            int num = parseInt(field);

            if (isYear(num))
            {
               if (year == 0) year = num;
            } else if (getAlphaMonth(field) > 0)
            {
               if (month == 0) {
                  month = getAlphaMonth(field);
               }
            } else if (isDay(num) && (!isNumMonth(num) ||
                  (i > 0 && getAlphaMonth(fields.get(i - 1)) > 0) ||
                  (i < fields.size() - 1 && getAlphaMonth(fields.get(i + 1)) > 0)))
            {
               if (day == 0) {
                  day = num;
               }
            } else if (i > 0 && isYear(parseInt(fields.get(i - 1))))
            {
               // ignore -- probably 1963/4
            } else if (isNumMonth(num))
            {
               if (month == 0) {
                  month = num;
               }
            }
         }

         if (year > 0) {
            result = Integer.toString(year);
            if (result.length() < 4) result = "0000".substring(0, 4 - result.length()) + result;
            if (month > 0) {
               result += (month < 10 ? "0" +
                     Integer.toString(month) : Integer.toString(month));
               if (day > 0) {
                  result += (day < 10 ? "0" +
                        Integer.toString(day) : Integer.toString(day));
               }
            }
         }
      }
      return result;
   }

   // keep in sync with Utils.NAMESPACE_MAP in indexer project
   public static final String NS_PLACE_TEXT = "Place";
   public static final String NS_PERSON_TEXT = "Person";
   public static final String NS_SOURCE_TEXT = "Source";
   public static final String NS_ARTICLE_TEXT = "Article";
   public static final int NS_MAIN = 0;
   public static final int NS_USER = 2;
   public static final int NS_PROJECT = 4;
   public static final int NS_IMAGE = 6;
   public static final int NS_MEDIAWIKI = 8;
   public static final int NS_TEMPLATE = 10;
   public static final int NS_HELP = 12;
   public static final int NS_CATEGORY = 14;
   public static final int NS_GIVENNAME = 100;
   public static final int NS_SURNAME = 102;
   public static final int NS_SOURCE = 104;
   public static final int NS_PLACE = 106;
   public static final int NS_PERSON = 108;
   public static final int NS_FAMILY = 110;
   public static final int NS_MYSOURCE = 112;
   public static final int NS_REPOSITORY = 114;
   public static final int NS_PORTAL = 116;
   public static final int NS_TRANSCRIPT = 118;
   public static final Map<String,Integer> NAMESPACE_MAP = new HashMap<String,Integer>();
   static {
      NAMESPACE_MAP.put("Talk",NS_MAIN+1);
      NAMESPACE_MAP.put("User",NS_USER);
      NAMESPACE_MAP.put("User talk",NS_USER+1);
      NAMESPACE_MAP.put("WeRelate",NS_PROJECT);
      NAMESPACE_MAP.put("WeRelate talk",NS_PROJECT+1);
      NAMESPACE_MAP.put("Image",NS_IMAGE);
      NAMESPACE_MAP.put("Image talk",NS_IMAGE+1);
      NAMESPACE_MAP.put("MediaWiki",NS_MEDIAWIKI);
      NAMESPACE_MAP.put("MediaWiki talk",NS_MEDIAWIKI);
      NAMESPACE_MAP.put("Template",NS_TEMPLATE);
      NAMESPACE_MAP.put("Template talk",NS_TEMPLATE+1);
      NAMESPACE_MAP.put("Help",NS_HELP);
      NAMESPACE_MAP.put("Help talk",NS_HELP+1);
      NAMESPACE_MAP.put("Category",NS_CATEGORY);
      NAMESPACE_MAP.put("Category talk",NS_CATEGORY+1);
      NAMESPACE_MAP.put("Givenname",NS_GIVENNAME);
      NAMESPACE_MAP.put("Givenname talk",NS_GIVENNAME+1);
      NAMESPACE_MAP.put("Surname",NS_SURNAME);
      NAMESPACE_MAP.put("Surname talk",NS_SURNAME+1);
      NAMESPACE_MAP.put("Source",NS_SOURCE);
      NAMESPACE_MAP.put("Source talk",NS_SOURCE+1);
      NAMESPACE_MAP.put(NS_PLACE_TEXT,NS_PLACE);
      NAMESPACE_MAP.put("Place talk",NS_PLACE+1);
      NAMESPACE_MAP.put(NS_PERSON_TEXT,NS_PERSON);
      NAMESPACE_MAP.put("Person talk",NS_PERSON+1);
      NAMESPACE_MAP.put("Family",NS_FAMILY);
      NAMESPACE_MAP.put("Family talk",NS_FAMILY+1);
      NAMESPACE_MAP.put("MySource",NS_MYSOURCE);
      NAMESPACE_MAP.put("MySource talk",NS_MYSOURCE+1);
      NAMESPACE_MAP.put("Repository",NS_REPOSITORY);
      NAMESPACE_MAP.put("Repository talk",NS_REPOSITORY+1);
      NAMESPACE_MAP.put("Portal",NS_PORTAL);
      NAMESPACE_MAP.put("Portal talk",NS_PORTAL+1);
      NAMESPACE_MAP.put("Transcript",NS_TRANSCRIPT);
      NAMESPACE_MAP.put("Transcript talk",NS_TRANSCRIPT+1);
   }

   public static String[] splitNamespaceTitle(String fullTitle) {
      String[] fields = new String[2];
      fields[0] = "";
      fields[1] = fullTitle;

      int i = fullTitle.indexOf(":");
      if (i > 0) {
         String namespace = fullTitle.substring(0,i);
         Integer ns = NAMESPACE_MAP.get(namespace);
         if (ns != null) {
            fields[0] = namespace;
            fields[1] = fullTitle.substring(i+1);
         }
      }
      return fields;
   }

   public static String prepareWikiTitle(String title) {
      return prepareWikiTitle(title, MAX_TITLE_LEN);
   }

   /**
    * Convert a string into a form that can be used for a wiki title
    * @param title string to turn into a wiki title
    * @param maxTitleLen max length
    * @return wiki title
    */
   public static String prepareWikiTitle(String title, int maxTitleLen) {
      title = title.replace('<','(').replace('[','(').replace('{','(').replace('>',')').replace(']',')').replace('}',')').
                    replace('|','-').replace('_',' ').replace('/','-').replace("#"," ").replace("?", " ").replace("+"," and ").replace("&"," and ");
      title = title.replaceAll("%([0-9a-fA-F][0-9a-fA-F])", "% $1").replaceAll("\\s+", " ").replaceAll("//+", "/").trim();
      StringBuffer dest = new StringBuffer();
      for (int i = 0; i < title.length(); i++) {
         char c = title.charAt(i);
         // omit control characters, unicode unknown character
         if ((int)c >= 32 && c != 0xFFFD &&
            !(c == 0x007F) &&
            // omit Hebrew characters (right-to-left)
            !(c >= 0x0590 && c <= 0x05FF) && !(c >= 0xFB00 && c <= 0xFB4F) &&
            // omit Arabic characters (right-to-left)
            !(c >= 0x0600 && c <= 0x06FF) && !(c >= 0x0750 && c <= 0x077F) && !(c >= 0xFB50 && c <= 0xFC3F) && !(c >= 0xFE70 && c <= 0xFEFF)
         ) {
            dest.append(c);
         }
      }
      title = dest.toString().trim();
      while (title.length() > 0 &&
              (title.charAt(0) == ' ' || title.charAt(0) == '.' || title.charAt(0) == '/' ||
               title.charAt(0) == ':' || title.charAt(0) == '-' || title.charAt(0) == ',')) {
         title = title.substring(1);
      }
      if (title.length() > maxTitleLen) {
         int pos = maxTitleLen;
         if (maxTitleLen > 20) {
            while (maxTitleLen - pos < 20 && title.charAt(pos) != ' ') pos--;
         }
         title = title.substring(0, pos).trim();
      }
      return uppercaseFirstLetter(title);
   }

   /**
    * Uppercase the first letter (only) of the specified string
    * @param in string to convert
    * @return string wtih first letter uppercased
    */
   public static String uppercaseFirstLetter(String in) {
      if (in.length() > 0 && Character.isLowerCase(in.charAt(0))) {
         return in.substring(0,1).toUpperCase() + in.substring(1);
      }
      return in;
   }

   /**
    * Convert non-roman but roman-like letters in the specified string to their roman (a-zA-Z) equivalents.
    * For example, strip accents from characters, and expand ligatures.
    * From Ancestry names code by Lee Jensen and Dallan Quass
    * @param s string to romanize
    * @return romanized word, may contain non-roman characters from non-roman-like alphabets like greek, arabic, hebrew
    */
   public static String romanize(String s) {
      if (s == null) {
         return "";
      }
      if (isAscii(s)) {
         return s;
      }

      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         String replacement;
         if ((int)c > 127 && (replacement = CHARACTER_MAPPINGS.get(c)) != null) {
            buf.append(replacement);
         }
         else {
            buf.append(c);
         }
      }
      return buf.toString();
   }

   private static final String[] CHARACTER_REPLACEMENTS = {
      "æ","ae",
      "ǝ","ae",
      "ǽ","ae",
      "ǣ","ae",
      "Æ","Ae",
      "Ə","Ae",
      "ß","ss",
      "đ","dj",
      "Đ","Dj",
      "ø","oe",
      "œ","oe",
      "Œ","Oe",
      "Ø","Oe",
      "þ","th",
      "Þ","Th",
      "ĳ","y",
      "Ĳ","Y",
      "á","a",
      "à","a",
      "â","a",
      "ä","a",
      "å","a",
      "ą","a",
      "ã","a",
      "ā","a",
      "ă","a",
      "ǎ","a",
      "ȃ","a",
      "ǻ","a",
      "ȁ","a",
      "Ƌ","a",
      "ƌ","a",
      "ȧ","a",
      "Ã","A",
      "Ą","A",
      "Á","A",
      "Ä","A",
      "Å","A",
      "À","A",
      "Â","A",
      "Ā","A",
      "Ă","A",
      "Ǻ","A",
      "ĉ","c",
      "ć","c",
      "č","c",
      "ç","c",
      "ċ","c",
      "Ĉ","C",
      "Č","C",
      "Ć","C",
      "Ç","C",
      "ð","d",
      "ď","d",
      "Ď","D",
      "Ð","D",
      "Ɖ","D",
      "ê","e",
      "é","e",
      "ë","e",
      "è","e",
      "ę","e",
      "ė","e",
      "ě","e",
      "ē","e",
      "ĕ","e",
      "ȅ","e",
      "Ė","E",
      "Ę","E",
      "Ê","E",
      "Ë","E",
      "É","E",
      "È","E",
      "Ě","E",
      "Ē","E",
      "Ĕ","E",
      "ƒ","f",
      "ſ","f",
      "ğ","g",
      "ģ","g",
      "ǧ","g",
      "ġ","g",
      "Ğ","G",
      "Ĝ","G",
      "Ģ","G",
      "Ġ","G",
      "Ɠ","G",
      "ĥ","h",
      "Ħ","H",
      "í","i",
      "і","i",
      "ī","i",
      "ı","i",
      "ï","i",
      "î","i",
      "ì","i",
      "ĭ","i",
      "ĩ","i",
      "ǐ","i",
      "į","i",
      "Í","I",
      "İ","I",
      "Î","I",
      "Ì","I",
      "Ï","I",
      "І","I",
      "Ĩ","I",
      "Ī","I",
      "ј","j",
      "ĵ","j",
      "Ј","J",
      "Ĵ","J",
      "ķ","k",
      "Ķ","K",
      "ĸ","K",
      "ł","l",
      "ŀ","l",
      "ľ","l",
      "ļ","l",
      "ĺ","l",
      "Ļ","L",
      "Ľ","L",
      "Ŀ","L",
      "Ĺ","L",
      "Ł","L",
      "ñ","n",
      "ņ","n",
      "ń","n",
      "ň","n",
      "ŋ","n",
      "ǹ","n",
      "Ň","N",
      "Ń","N",
      "Ñ","N",
      "Ŋ","N",
      "Ņ","N",
      "ô","o",
      "ö","o",
      "ò","o",
      "õ","o",
      "ó","o",
      "ő","o",
      "ơ","o",
      "ǒ","o",
      "ŏ","o",
      "ǿ","o",
      "ȍ","o",
      "ō","o",
      "ȯ","o",
      "ǫ","o",
      "Ó","O",
      "Ő","O",
      "Ô","O",
      "Ö","O",
      "Ò","O",
      "Õ","O",
      "Ŏ","O",
      "Ō","O",
      "Ơ","O",
      "Ƿ","P",
      "ƽ","q",
      "Ƽ","Q",
      "ř","r",
      "ŕ","r",
      "ŗ","r",
      "Ř","R",
      "Ʀ","R",
      "Ȓ","R",
      "Ŗ","R",
      "Ŕ","R",
      "š","s",
      "ś","s",
      "ş","s",
      "ŝ","s",
      "ș","s",
      "Ş","S",
      "Š","S",
      "Ś","S",
      "Ș","S",
      "Ŝ","S",
      "ť","t",
      "ţ","t",
      "ŧ","t",
      "ț","t",
      "Ť","T",
      "Ŧ","T",
      "Ţ","T",
      "Ț","T",
      "ũ","u",
      "ú","u",
      "ü","u",
      "ư","u",
      "û","u",
      "ů","u",
      "ù","u",
      "ű","u",
      "ū","u",
      "µ","u",
      "ǔ","u",
      "ŭ","u",
      "ȕ","u",
      "Ū","U",
      "Ű","U",
      "Ù","U",
      "Ú","U",
      "Ü","U",
      "Û","U",
      "Ũ","U",
      "Ư","U",
      "Ů","U",
      "Ǖ","U",
      "Ʊ","U",
      "ŵ","w",
      "Ŵ","W",
      "ÿ","y",
      "Ŷ","Y",
      "Ÿ","Y",
      "ý","y",
      "ȝ","y",
      "Ȝ","Y",
      "Ý","Y",
      "ž","z",
      "ź","z",
      "ż","z",
      "Ź","Z",
      "Ž","Z",
      "Ż","Z"
   };
   private static final HashMap<Character,String> CHARACTER_MAPPINGS = new HashMap<Character,String>();
   static {
      for (int i = 0; i < CHARACTER_REPLACEMENTS.length; i+=2) {
         CHARACTER_MAPPINGS.put(CHARACTER_REPLACEMENTS[i].charAt(0), CHARACTER_REPLACEMENTS[i+1]);
      }
   }
}
