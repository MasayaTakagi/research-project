package parser;

import java.io.File;
import java.io.IOException;

public interface LogParser {
	public void parseLog(File inputFile,File outputFile) throws IOException;
	public String getParserName();
        public void setTimeZone(int plusGMT);
}
