package edu.virginia.lib.findingaid.structure;

import edu.virginia.lib.findingaid.rules.AssignmentFragmentAction;
import edu.virginia.lib.findingaid.rules.BlockMatcher;
import edu.virginia.lib.findingaid.rules.ElementPattern;
import edu.virginia.lib.findingaid.rules.FragmentAction;
import edu.virginia.lib.findingaid.rules.Rule;
import edu.virginia.lib.findingaid.rules.SequentialPatternBlockMatcher;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class XmlSerializedProfile extends Profile {

    public static XmlSerializedProfile loadProfile(InputStream xmlStream) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(XmlSerializedProfile.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement<XmlSerializedProfile> p = unmarshaller.unmarshal(new StreamSource(xmlStream), XmlSerializedProfile.class);
        final XmlSerializedProfile s = p.getValue();
        for (XmlSerializedNodeType n : s.nodeType) {
            n.profile = s;
        }
        s.validate();
        return s;
    }


    @XmlElement private String name;

    @XmlElement private String description;

    @XmlElement private String rootNodeTypeId;

    @XmlElement private String transformation;

    @XmlElementWrapper(name="nodeTypes")
    @XmlElement private XmlSerializedNodeType[] nodeType;

    @XmlElementWrapper(name="rules")
    @XmlElement private XmlSerializedRule[] rule;

    private Map<String, NodeType> nodeTypeMap;

    private Transformer t;

    @Override
    public String getProfileName() {
        return name;
    }

    @Override
    public String getProfileDescription() {
        return description;
    }

    @Override
    public NodeType getRootNodeType() {
        return getNodeType(rootNodeTypeId);
    }

    private Map<String, NodeType> getNodeTypeMap() {
        if (nodeTypeMap == null) {
            // lazy load it
            nodeTypeMap = new HashMap<String, NodeType>();
            for (NodeType n : nodeType) {
                nodeTypeMap.put(n.getId(), n);
            }
        }
        return nodeTypeMap;
    }

    @Override
    public List<NodeType> getAssignedNodeTypes() {
        return new ArrayList<NodeType>(getNodeTypeMap().values());
    }

    @Override
    public List<Rule> getRules() {
        ArrayList<Rule> result = new ArrayList<Rule>();
        for (XmlSerializedRule r : rule) {
            result.add(r);
        }
        return result;
    }

    @Override
    public String transformDocument(Element el) {
        try {
            if (t == null) {
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Templates templates = tFactory.newTemplates(
                        new StreamSource(getClass().getClassLoader().getResourceAsStream(transformation)));
                t = templates.newTransformer();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                el.writeOutXML(baos);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
            baos.toString();
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder b = f.newDocumentBuilder();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.transform(new StreamSource(new ByteArrayInputStream(baos.toByteArray())), new StreamResult(out));
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validate() {
        for (NodeType nodeType : this.getAssignedNodeTypes()) {
            nodeType.possibleChildren();
        }
    }

    public boolean equals(Profile other) {
        return other.getClass().equals(getClass()) && this.name.equals(other.getProfileName());
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    private static class XmlSerializedNodeType implements NodeType {

        @XmlElement private String id;

        @XmlElement private String label;

        @XmlElement private String description;

        @XmlElement private String isTextNode;

        @XmlElement private String[] canHaveChild;

        private Profile profile;

        @Override
        public Profile getProfile() {
            return profile;
        }

        @Override
        public String getDisplayLabel() {
            return label;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isTextNode() {
            return isTextNode.equals("true");
        }

        @Override
        public boolean canBeChildOf(NodeType type) {
            return type.canHaveChild(this);
        }

        @Override
        public boolean canHaveChild(NodeType type) {
            if (!type.getProfile().equals(profile)) {
                return false;
            }
            if (canHaveChild == null) {
                return false;
            }
            for (String validChild : canHaveChild) {
                if (type.getId().equals(validChild)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<NodeType> possibleChildren() {
            ArrayList<NodeType> result = new ArrayList<NodeType>();
            if (canHaveChild == null) {
                return result;
            }
            for (String childId : canHaveChild) {
                final NodeType childType = profile.getNodeType(childId);
                if (childType == null) {
                    throw new IllegalStateException("Invalid profile, no node type defined for " + childId + "!");
                }
                result.add(profile.getNodeType(childId));
            }
            return result;
        }

        public boolean equals(NodeType other) {
            return this.profile.equals(other.getProfile()) && this.id.equals(other.getId());
        }

        public int hashCode() {
            return (this.profile.getProfileName() + "-" + this.getId()).hashCode();
        }

        public String toString() {
            return id;

        }

    }

    private static class XmlSerializedRule implements Rule {

        @XmlElement private String description;

        @XmlElementWrapper(name="match")
        @XmlElement private XmlSerializedBlock[] block;

        @XmlElementWrapper(name="action")
        @XmlElement private XmlSerializedAssignBlock[] assignblock;

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public BlockMatcher getWhenClause() {
            return block[0].getBlockMatcher();
        }

        @Override
        public FragmentAction getAction() {
            return assignblock[0].getAction();
        }
    }

    private static class XmlSerializedBlock {

        @XmlElement private XmlSerializedElementMatch[] elementmatch;

        public BlockMatcher getBlockMatcher() {
            ArrayList<ElementPattern> elementPatterns = new ArrayList<ElementPattern>();
            for (XmlSerializedElementMatch e : elementmatch) {
                elementPatterns.add(e);
            }
            return new SequentialPatternBlockMatcher(elementPatterns);
        }
    }

    private static class XmlSerializedAssignBlock {

        @XmlAttribute private String blockId;

        @XmlAttribute private String path;

        @XmlElement private XmlSerializedAssign[] assign;

        @XmlElement private XmlSerializedOmit[] omit;

        public AssignmentFragmentAction getAction() {

            Set<String> omitSet = new HashSet<String>();
            if (omit != null) {
                for (XmlSerializedOmit o : omit) {
                    omitSet.add(o.matchedId);
                }
            }

            Map<String, String> idToPathMap = new HashMap<String, String>();
            if (assign != null) {
                for (XmlSerializedAssign a : assign) {
                    idToPathMap.put(a.matchedId, a.path);
                }
            }
            return new AssignmentFragmentAction(path, idToPathMap, omitSet);

        }
    }

    private static class XmlSerializedElementMatch implements ElementPattern {

        @XmlAttribute private String position;

        @XmlAttribute private String type;

        @XmlAttribute private String id;

        @XmlAttribute private String qualifier;

        @XmlAttribute private String inverse;

        @XmlValue private String pattern;

        @Override
        public String getName() {
            return id;
        }

        @Override
        public Pattern getPattern() {
            return Pattern.compile(pattern, Pattern.DOTALL);
        }

        @Override
        public String getPosition() {
            if (position == null) {
                return "any";
            } else {
                return position;
            }

        }

        @Override
        public boolean matchesMultiple() {
            return "+".equals(qualifier);
        }

        @Override
        public boolean inverse() {
            return "true".equalsIgnoreCase(inverse);
        }
    }

    private static class XmlSerializedAssign {

        @XmlAttribute private String matchedId;
        @XmlAttribute private String path;
    }

    private static class XmlSerializedOmit {

        @XmlAttribute private String matchedId;
    }
}
