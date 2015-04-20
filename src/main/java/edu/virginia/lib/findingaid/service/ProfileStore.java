package edu.virginia.lib.findingaid.service;

import edu.virginia.lib.findingaid.structure.Profile;
import edu.virginia.lib.findingaid.structure.XmlSerializedProfile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileStore {

    private List<Profile> profileList;

    public ProfileStore() throws IOException, JAXBException {
        profileList = new ArrayList<Profile>();
        profileList.add(XmlSerializedProfile.loadProfile(getClass().getClassLoader().getResourceAsStream("uvaead-schema.xml")));
        profileList.add(XmlSerializedProfile.loadProfile(getClass().getClassLoader().getResourceAsStream("modern-library-tei.xml")));
    }

    public Profile getDefaultProfile() {
        return profileList.get(0);
    }

    public List<Profile> getProfileList() {
        return profileList;
    }

    public Profile getProfile(String name) {
        for (Profile s : profileList) {
            if (s.getProfileName().equals(name)) {
                return s;
            }
        }
        return null;
    }


    private static ProfileStore PROFILE_STORE = null;

    public static ProfileStore getProfileStore() {
        try {
            if (PROFILE_STORE == null) {
                PROFILE_STORE = new ProfileStore();
            }
            return PROFILE_STORE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
