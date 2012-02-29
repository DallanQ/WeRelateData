package org.folg.werelatedata.examples;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import org.folg.werelatedata.parser.Util;
import org.folg.werelatedata.parser.WikiParser;
import org.folg.werelatedata.parser.WikiReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * User: dallan
 * Date: 2/29/12
 */
public class FindMultiSpouseFamilies extends WikiParser {
   private PrintWriter outSameGender = null;
   private PrintWriter outDiffGender = null;

   public void parse(String title, String text, int pageId, int latestRevId, String username, String timestamp, String comment)
           throws IOException, ParsingException {
      if (title.startsWith("Family:")) {
         String[] split = splitStructuredWikiText("family", text);
         String structuredData = split[0];
         if (!Util.isEmpty(structuredData)) {
            Element root = parseText(structuredData).getRootElement();
            Elements wives = root.getChildElements("wife");
            Elements husbands = root.getChildElements("husband");

            if (wives.size() > 0 && husbands.size() > 0 && wives.size() + husbands.size() > 2) {
               outDiffGender.println("* [["+title+"]]");
            }
            else if ((wives.size() == 0 || husbands.size() == 0) && wives.size() + husbands.size() > 1) {
               outSameGender.println("* [["+title+"]]");
            }
         }
      }
   }

   private void setOutput(PrintWriter outSameGender, PrintWriter outDiffGender) {
      this.outSameGender = outSameGender;
      this.outDiffGender = outDiffGender;
   }

   // Generate lists of families that have unlikely spouse combinations: three or more, or two husbands/wives and zero of the other
   // args array: 0=pages.xml 1=samegender.wiki 2=diffgender.wiki
   public static void main(String[] args)
           throws IOException, ParsingException
   {
      if (args.length != 3) {
         System.out.println("Usage: <pages file in> <same gender wikitext out> <different gender wikitext out>");
         System.exit(1);
      }

      WikiReader wikiReader = new WikiReader();
      wikiReader.setSkipRedirects(true);
      FindMultiSpouseFamilies self = new FindMultiSpouseFamilies();
      PrintWriter outSameGender = new PrintWriter(args[1]);
      PrintWriter outDiffGender = new PrintWriter(args[2]);
      self.setOutput(outSameGender, outDiffGender);
      wikiReader.addWikiParser(self);
      InputStream in = new FileInputStream(args[0]);
      wikiReader.read(in);
      in.close();
      outSameGender.close();
      outDiffGender.close();
   }
}
