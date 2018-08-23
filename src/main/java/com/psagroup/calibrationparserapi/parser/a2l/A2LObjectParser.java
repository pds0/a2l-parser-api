package com.psagroup.calibrationparserapi.parser.a2l;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class A2LObjectParser {

    /**
     * Parse an .a2l object by putting all its file content in a String with separator ("\s" or "\n")
     *
     * @param reader      the bufferReader use to read the file it's used read line by line
     * @param currentLine the current line of the .a2l file being read in the loop
     * @throws IOException in case of failure when reading the file
     */
    abstract void parse(BufferedReader reader, String currentLine) throws IOException;

    /**
     * Get the next line in the file
     *
     * @param reader the bufferReader use to read the file it's used read line by line
     * @return the next line in the .a2l object
     * @throws IOException in case of failure when reading the file
     */
    public String next(BufferedReader reader) throws IOException {
        String nextLine = reader.readLine().trim();
        while (nextLine.isEmpty()) {
            nextLine = reader.readLine().trim();
        }
        return nextLine;
    }


    /**
     * @param str
     * @return
     */
    String[] divideContent(String str) {
        int cnt = 0;
        Pattern ptrn = Pattern.compile("\\s+(\".*?\")\\n|\\S+");
        Matcher matcher = ptrn.matcher(str);
        while (matcher.find()) {
            cnt++;
        }

        String[] result = new String[cnt];
        matcher.reset();
        int idx = 0;
        while (matcher.find()) {
            result[idx] = matcher.group(0).replaceAll("\"", "").replaceAll("\n", "").trim();
            idx++;
        }
        return result;
    }
}
