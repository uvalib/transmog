package edu.virginia.lib.findingaid;

import edu.virginia.lib.findingaid.rules.BlockMatch;
import edu.virginia.lib.findingaid.rules.ElementMatch;
import edu.virginia.lib.findingaid.rules.ElementPattern;
import edu.virginia.lib.findingaid.rules.SequentialPatternBlockMatcher;
import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Profile;
import edu.virginia.lib.findingaid.structure.XmlSerializedProfile;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SequentialPatternBlockMatcherTest {

    private Profile s;

    @Before
    public void setup() throws JAXBException {
        s = XmlSerializedProfile.loadProfile(getClass().getClassLoader().getResourceAsStream("test-schema-1.xml"));
    }

    @Test
    public void testMatchAll() {
        SequentialPatternBlockMatcher m = new SequentialPatternBlockMatcher(Arrays.asList(new ElementPattern[] { new SimpleElementPattern("Match All", ".*", "any", true, false) }));

        List<BlockMatch> matches = m.findMatches(createDummyElementsWithContent("one", "two", "three"));
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(3, matches.get(0).getMatches().size());
    }

    /**
     * A test to see if all the matches for "moo" are found in "moomoomoo".
     */
    @Test
    public void testMatchSubsequent() {
        SequentialPatternBlockMatcher m = new SequentialPatternBlockMatcher(Arrays.asList(new ElementPattern[] {
                new SimpleElementPattern("Match m", "m", "any", false, false),
                new SimpleElementPattern("Match o's", "o", "any", true, false),}));

        List<BlockMatch> matches = m.findMatches(createDummyElementsWithContent("m", "o", "o", "m", "o", "o", "m", "o", "o"));
        for (BlockMatch match : matches) {
            System.out.print("Match: \"");
            for (ElementMatch em : match) {
                System.out.print(em.getElement().getContentAsString());
            }
            System.out.println("\"");
        }
        Assert.assertEquals(3, matches.size());
    }

    @Test
    public void testTwoRequired() {
        SequentialPatternBlockMatcher m = new SequentialPatternBlockMatcher(Arrays.asList(new ElementPattern[] {
                new SimpleElementPattern("Match m", "m", "any", false, false),
                new SimpleElementPattern("Match o", "o", "any", false, false),}));
        List<BlockMatch> matches = m.findMatches(createDummyElementsWithContent("m", "o", "o", "o", "o", "o", "o"));
        Assert.assertEquals(1, matches.size());
    }

    private Element createDummyElementsWithContent(final String ... contents) {
        Element el = new Element(s.getRootNodeType());
        ArrayList<Element> result = new ArrayList<Element>();
        for (String c : contents) {
            el.addChild(new Element(s.getUnassignedType(), c));
        }
        return el;
    }

    private class SimpleElementPattern implements ElementPattern {

        String name;
        String pattern;
        String position;
        boolean matchesMultiple;
        boolean inverse;

        public SimpleElementPattern(String name, String pattern, String position, boolean matchesMultiple, boolean inverse) {
            this.name = name;
            this.pattern = pattern;
            this.position = position;
            this.matchesMultiple = matchesMultiple;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile(pattern);
        }

        @Override
        public String getPosition() {
            return position;
        }

        @Override
        public boolean matchesMultiple() {
            return matchesMultiple;
        }

        @Override
        public boolean inverse() {
            return inverse;
        }
    }

}
