# Instructions

## Requirements

- A recent version of Java
- A browser with WebSockets support

## Starting the components

Start the server by running `java -jar target/sieserver-0.1.0-SNAPSHOT-standalone.jar` (note the /, may have to change to \ on Windows) when in the sie-server directory. The server should print "WebSorket server started on port 9090" after a few seconds.

To start the client, simply open `client.html` in a browser, on the same machine (the client will try to connect to ws://localhost:9090).

# Additional Information and Comments

- There is a 32 MiB limit on file sizes, but that can be changed in the server's code.
- Large files can take a few seconds to upload, but the client will not indicate that it is waiting.
- The server is saving the uploaded files to disc, because (in a read-world scenario) they could come in handy if one were to extend or bug fix the program. In that case, the client would not have to upload the files again.
- If more than one instance of the client is open, they will mirror each other, not only on uploads, but on removals too.
- I included the target directory with .jars so you would not need leiningen.
