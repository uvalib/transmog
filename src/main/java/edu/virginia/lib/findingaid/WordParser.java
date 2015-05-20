package edu.virginia.lib.findingaid;

import edu.virginia.lib.findingaid.structure.Element;
import edu.virginia.lib.findingaid.structure.Fragment;
import edu.virginia.lib.findingaid.structure.Profile;
import org.apache.poi.POIXMLException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.IRunElement;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.apache.poi.xwpf.usermodel.VerticalAlign.SUBSCRIPT;

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

    private void processParagraph(XWPFParagraph p, Element root, Profile profile) {

        boolean recognizedFormatting = false;
        for (IRunElement i : p.getIRuns()) {
            XWPFRun r = (XWPFRun) i;
            if (r.isItalic() || r.isBold()) {
                recognizedFormatting = true;
                break;
            }
        }

        if (recognizedFormatting) {
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
                    plainText.append(r.toString());
                } else {
                    if (plainText.length() > 0) {
                        currentEl.addFragment(new Fragment(plainText.toString()));
                        plainText = new StringBuffer();
                    }
                    currentEl.addFragment(new Fragment(r.toString(), styles.toArray(new String[0])));
                }
            }
            if (plainText.length() > 0) {
                currentEl.addFragment(new Fragment(plainText.toString()));
            }
            if (currentEl.getContentAsString().trim().length() > 0) {
                root.addChild(currentEl);
            }
        } else {
            if (p.getText().trim().length() > 0) {
                root.addChild(new Element(profile.getUnassignedType(), p.getText()));
            }
        }

    }

}
