package edu.virginia.lib.findingaid.structure;

import edu.virginia.lib.findingaid.service.ProfileStore;
import junit.framework.Assert;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class DocumentTest {

    @Test
    public void testDocumentRoundtripping() throws XMLStreamException, IOException {
        Profile p = ProfileStore.getProfileStore().getDefaultProfile();
        Element root = new Element(p.getRootNodeType());
        addRandomChildren(5, root, 3);
        Document orig = new Document(root, "filename.docx");
        final String origStr = serializeToString(orig);
        Document copy = Document.parse(new ByteArrayInputStream(origStr.getBytes("UTF-8")), ProfileStore.getProfileStore());
        final String copyStr = serializeToString(copy);

        Assert.assertEquals("Roundtrip must be lossless.", origStr, copyStr);
        Assert.assertEquals("Roundtrip must preserve filename.", orig.getOriginalFilename(), copy.getOriginalFilename());
        Assert.assertEquals("Roundtrip must preserve profile name.", orig.getProfile().getProfileName(), copy.getProfile().getProfileName());
        Assert.assertEquals("Roundtrip must preserve element tree.", orig.getRootElement().printTreeXHTML(), copy.getRootElement().printTreeXHTML());
        Assert.assertTrue("Roundtrip must be lossless.", orig.equals(copy));

    }

    private void addRandomChildren(int number, Element el, int maxDepth) {
        if (maxDepth <= 0) {
            return;
        }
        for (int i = 0; i < number; i ++) {
            List<NodeType> possibleChildren = el.getType().possibleChildren();
            if (!possibleChildren.isEmpty()) {
                Element newChild = new Element(possibleChildren.get(new Random().nextInt(possibleChildren.size())));
                el.addChild(newChild);
                addRandomChildren(number, newChild, maxDepth - 1);
            }
            if (el.getType().isTextNode()) {
                el.addFragment(new Fragment("text", "random number " + Math.random()));
            }
        }

    }

    private String serializeToString(Document d) throws XMLStreamException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        d.serialize(baos);
        baos.close();
        return new String(baos.toByteArray(), "UTF-8");
    }



}
