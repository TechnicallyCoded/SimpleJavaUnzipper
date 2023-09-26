# Simple Java Unzipper

## Build
`gradlew build`

## Run
`java -jar SimpleUnzipper-<version>.jar [zip-file-name] [destination-folder] [verbose]`

 - zip-file-name (optional): Specify a file name to be used as the source zip folder. By default, if no file is specified, the program will find the first ".zip" in the current working directory.
 - destination-folder (optional): Specify a folder to place all the files into. By default, the program will use the current working directory.
 - verbose (optional): Valid options are "true" or "false". Verbose printing is enabled by default.
