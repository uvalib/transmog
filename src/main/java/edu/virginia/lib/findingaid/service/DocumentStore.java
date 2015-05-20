package edu.virginia.lib.findingaid.service;


import edu.virginia.lib.findingaid.structure.Document;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DocumentStore {

    private File storageDir;

    public DocumentStore(File dir) throws IOException {
        this.storageDir = dir;
    }

    /**
     * Stores a new document with the given id and returns that id.
     */
    public String addDocument(Document document) {
        getStorageDir().mkdirs();
        try {
            writeNewVersion(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return document.getId();
    }

    public void saveDocument(Document document) {
        try {
            writeNewVersion(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeNewVersion(Document document) throws IOException {
        final File docDir = getDirForDoc(document.getId());
        docDir.mkdirs();
        File outputFile = new File(docDir, "current");
        FileOutputStream fos = new FileOutputStream(outputFile);
        try {
            document.serialize(fos);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            fos.close();
        }
    }

    private File getStorageDir() {
        return storageDir;
    }

    private File getDirForDoc(String docId) {
        return new File(storageDir, docId);
    }

    /**
     * Fetches a document by id.
     */
    public Document getDocument(String id) {
        File docFile = new File(getDirForDoc(id), "current");
        if (!docFile.exists()) {
            return null;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(docFile);
            return Document.parse(fis, ProfileStore.getProfileStore());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static DocumentStore DOCUMENT_STORE = null;

    public static DocumentStore getDocumentStore() {
        try {
            if (DOCUMENT_STORE == null) {
                DOCUMENT_STORE = new DocumentStore(new File("DocumentStore"));
            }
            return DOCUMENT_STORE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
