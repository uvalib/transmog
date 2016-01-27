package edu.virginia.lib.findingaid;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.virginia.lib.findingaid.service.ProfileStore;
import edu.virginia.lib.findingaid.structure.Element;

public class WordParserTest {

    @Test
    public void testNonBreakingHyphen() throws URISyntaxException {
        WordParser p = new WordParser();
        Element e = p.processDocument(new File(WordParserTest.class.getClassLoader().getResource("non-breaking-hyphen.docx").toURI()), ProfileStore.getProfileStore().getDefaultProfile());
        assertEquals("Before\u2011After", e.getChildren().get(0).getContentAsString());
    }
}
