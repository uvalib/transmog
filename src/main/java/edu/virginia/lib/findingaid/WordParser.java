package edu.virginia.lib.findingaid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.apache.poi.POIXMLException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.IRunElement;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTEmpty;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPTab;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Fragment;
import edu.virginia.lib.findingaid.structure.Profile;

public class WordParser {

    public Element processDocument(File f, Profile s) {
        try {
            try {
                XWPFDocument d = new XWPFDocument(new FileInputStream(f));
                return processXMLDoc(s, d);
            } catch (POIXMLException e) {
                HWPFDocument wd = new HWPFDocument(new FileInputStream(f));
                return processWORDDoc(s, wd);
            }
        } catch (Exception e) {
            System.out.println("Could not parse " + f.getName());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A versy simple process for old word documents.  The only structure currently retained
     * is the paragraphs.
     */
    private Element processWORDDoc(Profile profile, HWPFDocument doc) throws IOException, XMLStreamException {
        Element root = new Element(profile.getRootNodeType());

        WordExtractor w = new WordExtractor(doc);
        for (String p : w.getParagraphText()) {
            if (p.trim().length() > 0) {
                root.addChild(new Element(profile.getUnassignedType(), p));
            }
        }

        return root;
    }

    private Element processXMLDoc(Profile profile, XWPFDocument doc) throws IOException, XMLStreamException, XmlException {
        Element root = new Element(profile.getRootNodeType());

        XWPFHeaderFooterPolicy policy = new XWPFHeaderFooterPolicy(doc);
        if (policy == null || policy.getDefaultHeader() == null || policy.getDefaultHeader().getText() == null) {
            // skip header
        } else {
            root.addChild(new Element(profile.getUnassignedType(), policy.getDefaultHeader().getText()));
        }
        for (XWPFParagraph p : doc.getParagraphs()) {
            processParagraph(p, root, profile);
        }
        return root;
    }

    private static final char UNICODECHAR_NONBREAKING_HYPHEN = '\u2011';
    private static final char UNICODE_SOFT_HYPHEN = '\u00AD';
    
    private void processParagraph(XWPFParagraph p, Element root, Profile profile) {
        Element currentEl = new Element(profile.getUnassignedType());
        StringBuffer plainText = new StringBuffer();
        for (IRunElement i : p.getIRuns()) {
            XWPFRun r = (XWPFRun) i;
            ArrayList<String> styles = new ArrayList<>();
            if (r.isItalic()) {
                styles.add(Fragment.ITALIC);
            }
            if (r.isBold()) {
                styles.add(Fragment.BOLD);
            }
            if (r.isStrike()) {
                styles.add(Fragment.STRIKETHROUGH);
            }
            if (r.getSubscript().equals(VerticalAlign.SUBSCRIPT)) {
                styles.add(Fragment.SUBSCRIPT);
            }
            if (r.getSubscript().equals(VerticalAlign.SUPERSCRIPT)) {
                styles.add(Fragment.SUPERSCRIPT);
            }

            if (styles.isEmpty()) {
                plainText.append(getRunText(r.getCTR()));
            } else {
                if (plainText.length() > 0) {
                    currentEl.addFragment(new Fragment(plainText.toString()));
                    plainText = new StringBuffer();
                }
                currentEl.addFragment(new Fragment(getRunText(r.getCTR()), styles.toArray(new String[0])));
            }
        }
        if (plainText.length() > 0) {
            currentEl.addFragment(new Fragment(plainText.toString()));
        }
        if (currentEl.getContentAsString().trim().length() > 0) {
            root.addChild(currentEl);
        }
    }
    
    /**
     * The following method is a copy of the toString method in XWPFRun with added
     * support for non-breaking hyphens.
     */
    private static String getRunText(CTR run) {
        StringBuffer text = new StringBuffer();

        // Grab the text and tabs of the text run
        // Do so in a way that preserves the ordering
        XmlCursor c = run.newCursor();
        c.selectPath("./*");
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTText) {
                String tagName = o.getDomNode().getNodeName();
                // Field Codes (w:instrText, defined in spec sec. 17.16.23)
                //  come up as instances of CTText, but we don't want them
                //  in the normal text output
                if (!"w:instrText".equals(tagName)) {
                    text.append(((CTText) o).getStringValue());
                }
            }

            if (o instanceof CTPTab) {
                text.append("\t");
            }
            if (o instanceof CTBr) {
                text.append("\n");
            }
            if (o instanceof CTEmpty) {
                // Some inline text elements get returned not as
                //  themselves, but as CTEmpty, owing to some odd
                //  definitions around line 5642 of the XSDs
                // This bit works around it, and replicates the above
                //  rules for that case
                String tagName = o.getDomNode().getNodeName();
                if ("w:tab".equals(tagName)) {
                    text.append("\t");
                } else if ("w:br".equals(tagName)) {
                    text.append("\n");
                } else if ("w:cr".equals(tagName)) {
                    text.append("\n");
                } else if ("w:noBreakHyphen".equals(tagName)) {
                    text.append(WordParser.UNICODECHAR_NONBREAKING_HYPHEN);
                } else if ("w:softHyphen".equals(tagName)) {
                    text.append(WordParser.UNICODE_SOFT_HYPHEN);
                } else {
                    System.err.println("Unrecognized empty control character! " + tagName);
                }
            }
            if (o instanceof CTFtnEdnRef) {
                CTFtnEdnRef ftn = (CTFtnEdnRef)o;
                String footnoteRef = ftn.getDomNode().getLocalName().equals("footnoteReference") ?
                    "[footnoteRef:" + ftn.getId().intValue() + "]" : "[endnoteRef:" + ftn.getId().intValue() + "]";
                text.append(footnoteRef);
            }            
        }

        c.dispose();

        return text.toString();
    }

}
