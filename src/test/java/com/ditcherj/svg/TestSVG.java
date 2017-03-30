package com.ditcherj.svg;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

import static org.junit.Assert.*;

/**
 * Created by jon on 30/03/2017.
 */
public class TestSVG {

    private static final Logger logger = LoggerFactory.getLogger(TestSVG.class);

    @Test
    public void convertToSVG() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("unnamed.png");
        StringWriter writer = new StringWriter();

        new SVGImageConverter().parse(inputStream, writer);

        assertNotNull(writer);

        String output = writer.toString();
        logger.trace("output[{}]", output);

        assertTrue(!output.isEmpty());
    }
}
