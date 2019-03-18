package org.trv.alex.remotefilemanager.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ParserFactoryTest {

    @Test
    public void getParserInstanceBy_Correct_Name() {
        assertNotNull(ParserFactory.getInstance("Lighttpd"));
    }

    @Test
    public void getParserInstanceBy_Incorrect_Name() {
        assertNull(ParserFactory.getInstance("None"));
    }
}
