package edu.virginia.lib.findingaid.service;


import edu.virginia.lib.findingaid.structure.Document;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.Merger;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class DocumentStore {

    private File storageDir;

    public DocumentStore(File dir) throws IOException {
        this.storageDir = dir;
    }

    /**
     * Stores a new document with the given id and returns that id.
     */
    public String addDocument(Document document) {
        try {
            writeNewVersion(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        return document.getId();
    }

    public void saveDocument(Document document) {
        try {
            writeNewVersion(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public Document undoLastChange(Document document) {
        try {
            final File docDir = getDirForDoc(document.getId());
            if (!docDir.exists()) {
                return null;
            } else {
                Git git = Git.open(docDir);
                Iterator<RevCommit> it = git.log().setMaxCount(2).call().iterator();
                if (it.hasNext()) {
                    final RevCommit c = it.next();  // current version
                }
                if (it.hasNext()) {
                    RevCommit c = it.next(); // previous version
                    if (isUndo(git)) {
                        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD~1").call();
                    } else {
                        git.branchCreate().setName("undo").setStartPoint(c).call();
                        git.checkout().setName("undo").call();
                    }
                    return getDocument(document.getId());
                }
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoHeadException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void redoLastUndo(Document document) {

    }

    private void writeNewVersion(Document document) throws IOException, GitAPIException {
        final File docDir = getDirForDoc(document.getId());
        Git git = null;
        if (!docDir.exists()) {
            docDir.mkdirs();
            git = new InitCommand().setDirectory(docDir).call();
        } else {
            git = Git.open(docDir);
            completeUndo(git);
        }

        File outputFile = new File(docDir, "document.xml");
        FileOutputStream fos = new FileOutputStream(outputFile);
        try {
            document.serialize(fos);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            fos.close();
        }
        git.add().addFilepattern("document.xml").call();
        git.commit().setCommitter("transmog", "transmog@fake.fake").setMessage("Updated through application.").call();
    }

    private void completeUndo(Git git) {
        try {
            if (isUndo(git)) {
                git.checkout().setName("master").call();
                git.merge().setStrategy(MergeStrategy.THEIRS).include(git.getRepository().resolve("undo")).setCommit(true).call();
                git.branchDelete().setBranchNames("undo").call();
            }
        } catch (RuntimeException ex) {

        } catch (CheckoutConflictException e) {
            throw new RuntimeException(e);
        } catch (RefAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidRefNameException e) {
            throw new RuntimeException(e);
        } catch (RefNotFoundException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isUndo(Git git) throws IOException {
        String head = git.getRepository().getFullBranch();
        return head.equals("refs/heads/undo");
    }

    private File getDirForDoc(String docId) {
        return new File(storageDir, docId);
    }

    /**
     * Fetches a document by id.
     */
    public Document getDocument(String id) {
        File docFile = new File(getDirForDoc(id), "document.xml");
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
