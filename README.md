This project contains tools and examples for parsing data dumps from WeRelate.org.  The data dumps are available under a [Creative Commons Attribution-ShareAlike license](http://creativecommons.org/licenses/by-sa/3.0/).

Steps
-----

1. Download this project.  Make sure you have java and maven.
2. Download the latest data dump from [here](http://public.werelate.org/pages.xml.gz).
3. Look at the examples in src/main/java/org/folg/werelatedata/examples.
4. Extend one of the examples or add your own.
5. Build using maven: `mvn install`
6. Run using maven: `mvn exec:java -Dexec.mainClass=<fully-qualified classname> -Dexec.args="<args>"

Notes
-----

I haven't documented the XML structure of each page.
You can usually figure this out by doing a diff between revisions in the page history, but I know it's a pain.
I'll document it eventually, but if there's a particular namespace that you'd like me to document sooner
rather than later, please let me know.

Writing bots to update pages on WeRelate
----------------------------------------

If you want to write a bot to update pages on WeRelate, take a look at `AddFhlcIdToPlaces` or `DeletePages` for examples.
Once you've written the script and have tested it on a few pages, logging in with your username and password,
submit a pull request so I can take a look at your script.  If it looks good, I'll give you a "bot" account that you
can use for mass updates.

Other projects
--------------

Check out [other genealogy projects](https://github.com/DallanQ)
