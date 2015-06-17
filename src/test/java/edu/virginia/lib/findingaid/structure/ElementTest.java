package edu.virginia.lib.findingaid.structure;

import edu.virginia.lib.findingaid.service.ProfileStore;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by md5wz on 6/17/15.
 */
public class ElementTest {

    private Profile p;

    private Element testEl;

    @Before
    public void setup() {
        p = ProfileStore.getProfileStore().getDefaultProfile();
        Element rootEl = new Element(p.getRootNodeType());
        testEl = new Element(p.getUnassignedType(), Arrays.asList(new Fragment[]{
                new Fragment("f1", new String[]{}, "abc"),
                new Fragment("f2", new String[]{ "ITALIC" }, "def"),
                new Fragment("f3", new String[]{}, "ghi") }));
        rootEl.addChild(testEl);
    }


    @Test
    public void testSplitOffFirstFragment() {
        assertEquals("Test element should 3 fragments.", 3, testEl.fragments.size());
        testEl.splitOnText("abc");
        assertEquals("Element should be split in two.", 2, testEl.getParent().getChildren().size());
        final Element first = testEl.getParent().getChildren().get(0);
        assertEquals("Element should still be first child.", first.id, testEl.id);
        assertEquals("First element should have only one fragment.", 1, first.fragments.size());
        assertEquals("First element should have retained the first fragment unmodified.", "f1", first.fragments.get(0).getId());
        assertEquals("abc", first.getContentAsString());
        final Element second = testEl.getParent().getChildren().get(1);
        assertEquals("After element should have received two fragments.", 2, second.fragments.size());
        assertEquals("After element should have received the second fragment unmodified.", "f2", second.fragments.get(0).getId());
        assertEquals("After element should have received the third fragment unmodified.", "f3", second.fragments.get(1).getId());
        assertEquals("defghi", second.getContentAsString());
    }

    @Test
    public void testSplitOffLastFragment() {
        assertEquals("Test element should 3 fragments.", 3, testEl.fragments.size());
        testEl.splitOnText("ghi");
        assertEquals("Element should be split in two.", 2, testEl.getParent().getChildren().size());
        final Element first = testEl.getParent().getChildren().get(0);
        assertEquals("First element should have 2 fragments.", 2, first.fragments.size());
        assertEquals("First element should have the first fragment unmodified.", "f1", first.fragments.get(0).getId());
        assertEquals("First element should have the second fragment unmodified.", "f2", first.fragments.get(1).getId());
        assertEquals("abcdef", first.getContentAsString());

        final Element second = testEl.getParent().getChildren().get(1);
        assertEquals("Element should be the second child.", second.id, testEl.id);
        assertEquals("Second element should have received one fragment.", 1, second.fragments.size());
        assertEquals("Second element should have received the third fragment unmodified.", "f3", second.fragments.get(0).getId());
        assertEquals("ghi", second.getContentAsString());
    }

    @Test
    public void testSplitWholeText() {
        assertEquals("Test element should 3 fragments.", 3, testEl.fragments.size());
        testEl.splitOnText("abcdefghi");
        assertEquals("Element should not be split.", 1, testEl.getParent().getChildren().size());
        assertEquals("Element should be retained in tact.", testEl.id, testEl.getParent().getChildren().get(0).id);
    }

    @Test
    public void testSplitMiddleOfFragment() {
        assertEquals("Test element should 3 fragments.", 3, testEl.fragments.size());
        testEl.splitOnText("e");
        assertEquals("Element should be split in three.", 3, testEl.getParent().getChildren().size());
        final Element first = testEl.getParent().getChildren().get(0);
        assertEquals("First element should have 2 fragments.", 2, first.fragments.size());
        assertEquals("First element should have the first fragment unmodified.", "f1", first.fragments.get(0).getId());
        assertEquals("First element should new second fragment \"d\".", "d", first.fragments.get(1).getText());
        assertEquals("First element should have italic second fragment.", "ITALIC", first.fragments.get(1).getStylesAsSpaceDelimitedString());
        assertEquals("abcd", first.getContentAsString());

        final Element second = testEl.getParent().getChildren().get(1);
        assertEquals("Element should be the second child.", second.id, testEl.id);
        assertEquals("Second element should have received one fragment.", 1, second.fragments.size());
        assertEquals("Second element should new first fragment \"e\".", "e", second.fragments.get(0).getText());
        assertEquals("Second element should have italic first fragment.", "ITALIC", second.fragments.get(0).getStylesAsSpaceDelimitedString());
        assertEquals("e", second.getContentAsString());

        final Element third = testEl.getParent().getChildren().get(2);
        assertEquals("Third element should have received 2 fragments.", 2, third.fragments.size());
        assertEquals("Third element should new first fragment \"f\".", "f", third.fragments.get(0).getText());
        assertEquals("Third element should have italic first fragment.", "ITALIC", third.fragments.get(0).getStylesAsSpaceDelimitedString());
        assertEquals("Third element should have the third fragment unmodified.", "f3", third.fragments.get(1).getId());
        assertEquals("fghi", third.getContentAsString());
    }

    @Test
    public void testSplitMiddleOfFragments() {
        assertEquals("Test element should 3 fragments.", 3, testEl.fragments.size());
        testEl.splitOnText("cdefg");
        assertEquals("Element should be split in three.", 3, testEl.getParent().getChildren().size());
        final Element first = testEl.getParent().getChildren().get(0);
        assertEquals("First element should have 1 fragment.", 1, first.fragments.size());
        assertEquals("First element should new first fragment \"ab\".", "ab", first.fragments.get(0).getText());
        assertEquals("ab", first.getContentAsString());

        final Element second = testEl.getParent().getChildren().get(1);
        assertEquals("Element should be the second child.", second.id, testEl.id);
        assertEquals("Second element should have 3 fragments.", 3, second.fragments.size());
        assertEquals("Second element should new first fragment \"c\".", "c", second.fragments.get(0).getText());
        assertEquals("Second element should have retained second fragment.", "f2", second.fragments.get(1).getId());
        assertEquals("Second element should new third fragment \"g\".", "g", second.fragments.get(2).getText());
        assertEquals("cdefg", second.getContentAsString());

        final Element third = testEl.getParent().getChildren().get(2);
        assertEquals("Third element should have 1 fragment.", 1, third.fragments.size());
        assertEquals("Third element should new first fragment \"hi\".", "hi", third.fragments.get(0).getText());
        assertEquals("hi", third.getContentAsString());
    }
}
