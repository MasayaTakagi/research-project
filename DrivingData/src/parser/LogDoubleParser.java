package parser;

import java.io.File;
import java.io.IOException;

public interface LogDoubleParser {
	public void parseDoubleLog(File inputFile1,File inputFile2,File outputFile) throws IOException;
	public String getParserName();
        public void setTimeZone(int plusGMT);
}
