package org.folg.werelatedata.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.folg.werelatedata.parser.Util;

/*
* Copyright 2010 Foundation for On-Line Genealogy Inc.
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
public class PageEditor {
   public static final Pattern EDITTIME_PATTERN = Pattern.compile(
         "<input type='hidden' value=\"([^>]*?)\" name=\"wpEdittime\" />");
   public static final Pattern STARTTIME_PATTERN = Pattern.compile(
         "<input type='hidden' value=\"([^>]*?)\" name=\"wpStarttime\" />");
   public static final Pattern EDIT_TOKEN_PATTERN1 = Pattern.compile(
         "<input type='hidden' value=['\"]([^>]*?)['\"] name=['\"]wpEditToken['\"] />");
   public static final Pattern EDIT_TOKEN_PATTERN2 = Pattern.compile(
         "<input type='hidden' name=['\"]wpEditToken['\"] value=['\"]([^>]*?)['\"] />");
   public static final Pattern TEXTBOX1_PATTERN = Pattern.compile(
         "<textarea[^>]*?name=\"wpTextbox1\"[^>]*>(.*?)</textarea>", Pattern.DOTALL);

   private static final String NOT_LOGGED_IN = "sign in</a> to edit pages.";
   private static final String STILL_EDITING = "<h1 class=\"firstHeading\">Editing";
   private static int MAX_RETRIES = 15;
   private static int RETRY_WAIT_MILLIS = 20000;
   private static int TIMEOUT_MILLIS = 60000;
   private static Logger logger = Logger.getLogger("org.folg.werelatedata.editor");
   private static final int BUF_SIZE = 32 * 1024;
   private static final int MAX_BUF_SIZE = 64 * 1024 * 1024;

   private String baseUrl;
   private String agentUsername;
   private String agentPassword;
   private String mockContents;
   private String title;
   private String contents;
   boolean loggedIn;
   protected Map<String, String> variables;
   private HttpClient client;

   /**
    * A PageEditor is used to fetch and update wiki pages
    * @param host
    * @param agentUsername
    * @param agentPassword
    */
   public PageEditor(String host, String agentUsername, String agentPassword) {
      this.baseUrl = "http://" + host;
      this.agentUsername = agentUsername;
      this.agentPassword = agentPassword;
      this.mockContents = null;
      this.title = null;
      this.contents = null;
      this.loggedIn = false;
      this.variables = new HashMap<String, String>();

      resetHttpClient();
   }

   public void resetHttpClient() {
      this.client = new HttpClient();
      this.client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
      this.client.getParams().setParameter("http.socket.timeout", TIMEOUT_MILLIS);
      this.client.getParams().setParameter("http.connection.timeout", TIMEOUT_MILLIS);
   }

   public String getMockContents()
   {
      return mockContents;
   }

   /**
    * Set mock page contents for testing variable patterns
    * @param mockContents
    */
   public void setMockContents(String mockContents)
   {
      this.mockContents = mockContents;
   }

   /**
    * Return full page contents
    * @return
    */
   public String getContents() {
      return this.contents;
   }

   /**
    * Return the page contents matching the specified pattern
    * @param p
    * @param isRequired if true, throws an exception if variable not found
    * @return
    */
   public String readVariable(Pattern p, boolean isRequired) {
      Matcher m = p.matcher(contents);
      if (m.find()) {
         return Util.unencodeXML(m.group(1)); // we need to unencode just HTML entities, but this works fine
      }
      if (isRequired) {
         throw new RuntimeException("Pattern not found: " + p + " on page: "+title);
      }
      return null;
   }

   public String readVariable(Pattern p) {
      return readVariable(p, true);
   }

   public String readSelectVariable(Pattern p, boolean isRequired) {
      Matcher m = p.matcher(contents);
      if (m.find()) {
         if (m.group(0).contains("</select>")) {
            return ""; // no option selected
         }
         else {
            return Util.unencodeXML(m.group(1));
         }
      }
      if (isRequired) {
         throw new RuntimeException("Pattern not found: " + p + " on page: "+title);
      }
      return null;
   }

   public String readSelectVariable(Pattern p) {
      return readSelectVariable(p,true);
   }

   /**
    * Set a variable for posting
    * wpStarttime, wpEdittime, wpEditToken, and wpSave are set automatically
    * @param var
    * @param value
    */
   public void setPostVariable(String var, String value) {
      variables.put(var, value);
   }

   private String constructUrl(String title, String action, String extraParams)
   {
      StringBuilder url = new StringBuilder();
      url.append(baseUrl);
      url.append("/w/index.php?title=");
      try {
         url.append(URLEncoder.encode(title, "UTF-8"));
      }
      catch (UnsupportedEncodingException e) {
         // this will never happen
         logger.severe("Unsupported encoding exception for UTF-8!?");
      }
      if (!Util.isEmpty(action)) {
         url.append("&action=");
         url.append(action);
      }
      if (!Util.isEmpty(extraParams)) {
         url.append("&");
         url.append(extraParams);
      }
      return url.toString();
   }

   private String getResponse(HttpMethodBase m) throws IOException
   {
      InputStream s = m.getResponseBodyAsStream();
      int bytesRead = -1;
      int totalBytes = 0;
      int bytesToRead = BUF_SIZE;
      byte[] buf = new byte[BUF_SIZE];
      while (true) {
         bytesRead = s.read(buf, totalBytes, bytesToRead);
         if (bytesRead < 0) {
            break;
         }
         totalBytes += bytesRead;
         bytesToRead -= bytesRead;
         if (bytesToRead == 0) { // buffer full, so allocate more
            if (buf.length * 2 > MAX_BUF_SIZE) {
               throw new IOException("Response too long: "+m.getURI().toString());
            }
            byte[] temp = buf;
            buf = new byte[temp.length * 2];
            System.arraycopy(temp, 0, buf, 0, temp.length);
            bytesToRead = temp.length;
         }
      }
      if (totalBytes > 0) {
         return EncodingUtil.getString(buf, 0, totalBytes, m.getResponseCharSet());
      } else {
         return null;
      }
   }

   /**
    * Fetch the page
    * @param title
    * @param edit
    */
   public void doGet(String title, boolean edit) {
      doGet(title, edit, null);
   }

   public void doGet(String title, boolean edit, String extraParams) {
      this.title = title;
      String url = constructUrl(title, edit ? "edit" : null, extraParams);

      for (int i = 0; i < MAX_RETRIES; i++) {
         if (doGetHttp(url, edit)) {
            return;
         }
         Util.sleep(RETRY_WAIT_MILLIS);
         resetHttpClient();
      }
      throw new RuntimeException("Get failed: " + title);
   }

   private boolean doGetHttp(String url, boolean edit) {
      contents = null;
      variables.clear();
      if (!loggedIn) {
         loggedIn = login();
      }
      if (!loggedIn) {
         logger.severe("Not logged in after login attempt");
         return false; // error
      }

      if (!Util.isEmpty(getMockContents())) {
         contents = getMockContents();
      }
      else {
         GetMethod m = new GetMethod(url);
         try {
            client.executeMethod(m);
            if (m.getStatusCode() != 200) {
               logger.severe("Unexpected status code on get: " + m.getStatusCode());
               return false;
            }
            else {
               contents = getResponse(m);
               if(contents.contains(NOT_LOGGED_IN))
               {
                  loggedIn = false;
                  logger.warning("Not logged in");
                  return false;
               }
            }
         }
         catch (IOException e)
         {
            logger.warning("IOException on "+ url + " -> " + e);
            return false;
         }
         finally {
            m.releaseConnection();
         }
      }

      // set 3 variables
      String var = readVariable(EDIT_TOKEN_PATTERN1, false);
      if (var == null) var = readVariable(EDIT_TOKEN_PATTERN2, false);
      if (var != null) variables.put("wpEditToken", var);
      if (edit) {
         variables.put("wpEdittime", readVariable(EDITTIME_PATTERN));
         variables.put("wpStarttime", readVariable(STARTTIME_PATTERN));
         variables.put("wpSave", "Save page");
      }
      return true;
   }

   public void doPost() {
     doPost("submit", null);
   }

   public void doPost(String action, String extraParams) {
      if (variables.size() > 0) {
         String url = constructUrl(title, action, extraParams);
         for (int i = 0; i < MAX_RETRIES; i++) {
            if (doPostHttp(url)) {
               return;
            }
          Util.sleep(RETRY_WAIT_MILLIS);
          resetHttpClient();
         }
      }
      throw new RuntimeException("Post failed: " + title);
   }

   private boolean doPostHttp(String url) {
      if (!loggedIn) {
         loggedIn = login();
      }
      if (!loggedIn) {
         logger.severe("Not logged in after login attempt");
         return false;
      }
      PostMethod m = new PostMethod(url);
      NameValuePair[] nvps = new NameValuePair[variables.size()];
      int i = 0;
      for (String name : variables.keySet()) {
         String value = variables.get(name);
         nvps[i] = new NameValuePair(name, value);
         i++;
      }
      m.setRequestBody(nvps);
      try {
         client.executeMethod(m);
         if (m.getStatusCode() == 302) {
            url = m.getResponseHeader("Location").getValue();
            m.releaseConnection();
            m = new PostMethod(url);
            m.setRequestBody(nvps);
            client.executeMethod(m);
         }
         if (m.getStatusCode() != 200) {
            logger.severe("Unexpected status code on post: " + m.getStatusCode());
            return false;
         }
         else {
            contents = getResponse(m);
            if(contents.contains(NOT_LOGGED_IN))
            {
               loggedIn = false;
               logger.warning("Not logged in");
               return false;
            }
            else if (contents.contains(STILL_EDITING)) {
               logger.warning("Still editing");
               return false;
            }
         }
      }
      catch (IOException e)
      {
         logger.warning("IOException on "+ url + " -> " + e);
         return false;
      }
      finally {
         m.releaseConnection();
      }

      return true;
   }

   private boolean login() {
      String url = constructUrl("Special:Userlogin", "submitlogin", "type=login");
      logger.info("Logging in: " + url);
      PostMethod m = new PostMethod(url);
      NameValuePair [] nvp = {
            new NameValuePair("wpName", agentUsername),
            new NameValuePair("wpPassword", agentPassword),
            new NameValuePair("wpLoginattempt", "Log in")
      };
      m.setRequestBody(nvp);
      try {
         client.executeMethod(m);
         if (m.getStatusCode() == 302) {
            url = m.getResponseHeader("Location").getValue();
            m.releaseConnection();
            m = new PostMethod(url);
            m.setRequestBody(nvp);
            client.executeMethod(m);
         }
         if (m.getStatusCode() != 200) {
            logger.severe("Unexpected status code logging in: " + m.getStatusCode());
            return false;
         }
         else {
            String returnText = getResponse(m);
            if (returnText.indexOf("Login successful") == -1) {
               logger.severe("There was a problem logging in. Here is the text:\n\n" + returnText);
               return false;
            }
         }
      } catch (IOException e) {
         logger.severe("There was an IOException when executing this url: " + url + " -> " + e);
         return false;
      } finally {
         m.releaseConnection();
      }

      return true;
   }

}
