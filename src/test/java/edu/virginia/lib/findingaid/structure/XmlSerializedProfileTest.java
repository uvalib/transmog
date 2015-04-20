package edu.virginia.lib.findingaid.structure;

import junit.framework.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XmlSerializedProfileTest {

    @Test
    public void testBasicParsing() throws JAXBException {
        final Profile s = XmlSerializedProfile.loadProfile(XmlSerializedProfileTest.class.getClassLoader().getResourceAsStream("test-schema-1.xml"));
        Assert.assertEquals("test", s.getProfileName());
        Assert.assertEquals("A simple test structure.", s.getProfileDescription());

        final List<NodeType> types = s.getAssignedNodeTypes();
        Assert.assertEquals(3, types.size());

        final NodeType documentNodeType = s.getRootNodeType();
        Assert.assertNotNull("Failed to parse root node type.", documentNodeType);
        Assert.assertEquals("ROOT", documentNodeType.getId());
        Assert.assertEquals("root", documentNodeType.getDisplayLabel());
        Assert.assertEquals("The entire document.", documentNodeType.getDescription());
        Assert.assertFalse(documentNodeType.isTextNode());
        Assert.assertTrue(documentNodeType.canHaveChild(s.getNodeType("A")));
        Assert.assertTrue(documentNodeType.canHaveChild(s.getNodeType("B")));

        for (NodeType t : types) {
            Assert.assertFalse(documentNodeType.canBeChildOf(t));
        }

        Set<NodeType> unreferencedTypes = new HashSet<NodeType>(types);
        removeType(documentNodeType, unreferencedTypes);
        Assert.assertEquals("All assigned types must be able to be inserted somewhere!", Collections.emptySet(), unreferencedTypes);

    }

    private static void removeType(NodeType type, Collection<NodeType> types) {
        if (types.contains(type)) {
            types.remove(type);
            for (NodeType c : type.possibleChildren()) {
                removeType(c, types);
            }
        }

    }
}
