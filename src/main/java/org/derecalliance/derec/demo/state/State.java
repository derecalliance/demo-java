/*
 * Copyright (c) DeRec Alliance and its Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.derecalliance.derec.demo.state;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.derecalliance.derec.lib.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class State {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public class DisplayEntry {
        public String title;
        public String contents;
        public Object associatedObj;

        public DisplayEntry(String title, String contents, Object associatedObj) {
            this.title = title;
            this.contents = contents;
            this.associatedObj = associatedObj;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            DisplayEntry other = (DisplayEntry) obj;
            return this.title.equals(other.title)
                    && this.contents.equals(other.contents)
                    && Objects.equals(this.associatedObj, other.associatedObj)
                    && (obj instanceof DeRecSecret
                            ? (((DeRecSecret) obj).isRecovering() == ((DeRecSecret) this).isRecovering())
                            : true);
        }
    }

    String colorBackground = "#e0e0e0";
    String colorPrimary = "#ff0000";
    String colorSecondary = "#00ff00";
    public final int appWidth = 400;
    public final int appHeight = 700;
    public String testUserRole = "Unknown";

    /*
     * -----------------------------------------------------------------------
     * Helper data structures
     * -----------------------------------------------------------------------
     */
    DeRecHelper helper = null;
    public ObservableList<DeRecHelper.SharerStatus> sharerStatuses = FXCollections.observableArrayList();
    public ObservableList<DeRecHelper.Share> stateShares = FXCollections.observableArrayList();
    public ObservableList<DeRecHelper.Notification> helperNotifications = FXCollections.observableArrayList();
    public ArrayList<DisplayEntry> sharerTabSecretsList = new ArrayList<>();
    public SimpleIntegerProperty observableSharerTabSecretsUpdateCounter = new SimpleIntegerProperty(1);
    public ArrayList<DisplayEntry> sharerTabHelpersContents = new ArrayList<>();
    public SimpleIntegerProperty observableSharerTabHelpersUpdateCounter = new SimpleIntegerProperty(1);
    public ArrayList<DisplayEntry> sharerTabVersionsContents = new ArrayList<>();
    public SimpleIntegerProperty observableSharerTabVersionsUpdateCounter = new SimpleIntegerProperty(1);
    public ObservableList<DeRecHelper.Share> shares = FXCollections.observableArrayList();

    /*
     * -----------------------------------------------------------------------
     * Sharer data structures
     * -----------------------------------------------------------------------
     */
    DeRecSharer sharer = null;
    //    public ObservableList<DeRecSecret> secrets = FXCollections.observableArrayList();
    public ArrayList<DeRecSecret> secrets = new ArrayList<>();

    public ObservableList<DeRecHelperStatus> helperStatusesForSelectedSecret = FXCollections.observableArrayList();
    public ObservableList<DeRecVersion> versionsForSelectedSecret = FXCollections.observableArrayList();
    public ObservableList<DeRecStatusNotification> sharerNotifications = FXCollections.observableArrayList();

    /*
     * -----------------------------------------------------------------------
     * Common data structures
     * -----------------------------------------------------------------------
     */
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm:ss");

    private static final State instance = new State();

    public class Selections {
        String name;
        String uri;
        DeRecSecret secret;
        DeRecVersion version;
        boolean paused;
        public BooleanProperty isRecovering = new SimpleBooleanProperty(false);

        // This variable keeps track of which of the tabs in the Sharer screen is selected.
        public enum SharerSelectedTab {
            Versions,
            Helpers,
        };

        SharerSelectedTab sharerSelectedTab = SharerSelectedTab.Helpers;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public DeRecSecret getSecret() {
            return secret;
        }

        public void setSecret(DeRecSecret secret) {
            this.secret = secret;
        }

        public DeRecVersion getVersion() {
            return version;
        }

        public void setVersion(DeRecVersion version) {
            this.version = version;
        }

        public boolean isRecovering() {
            return isRecovering.get();
        }

        public void setRecovering(boolean recovering) {
            isRecovering.set(recovering);
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public SharerSelectedTab getSharerSelectedTab() {
            return sharerSelectedTab;
        }

        public void setSharerSelectedTab(SharerSelectedTab sharerSelectedTab) {
            this.sharerSelectedTab = sharerSelectedTab;
        }
    }

    Selections userSelections = new Selections();

    // private constructor to avoid client applications using the constructor
    private State() {}

    public static State getInstance() {
        return instance;
    }

    public Selections getUserSelections() {
        return userSelections;
    }

    public DeRecHelper getHelper() {
        return helper;
    }

    public void setHelper(DeRecHelper helper) {
        this.helper = helper;
    }

    public DeRecSharer getSharer() {
        return sharer;
    }

    public void setSharer(DeRecSharer sharer) {
        this.sharer = sharer;
    }

    <T> void updateFromLibMethod(List<T> fromLib, ObservableList<T> stateVar) {
        // Add new items from fromLib to stateVar
        for (T libElem : fromLib) {
            if (!stateVar.contains(libElem)) {
                System.out.println("updateFromLibMethod Added this element: " + libElem);
                stateVar.add(libElem);
            }
        }
        // Remove items from stateVar that are not in fromLib
        List<T> itemsToRemove = new ArrayList<>();
        for (T stateElem : stateVar) {
            if (!fromLib.contains(stateElem)) {
                itemsToRemove.add(stateElem);
            }
        }
        stateVar.removeAll(itemsToRemove);
    }

    public void updateHelperFromLib() {
        if (helper == null) {
            return;
        }
        updateFromLibMethod((List<DeRecHelper.SharerStatus>) helper.getSharers(), sharerStatuses);
        System.out.println("Helper got " + sharerStatuses.size() + " sharer statuses");
        for (DeRecHelper.SharerStatus ss : sharerStatuses) {
            System.out.println("Sharer status: " + ss.getId().getName() + ", PEK: "
                    + ss.getId().getPublicEncryptionKey() + ", Obj: " + ss);
        }

        updateFromLibMethod((List<DeRecHelper.Share>) helper.getShares(), stateShares);
        System.out.println("Helper got " + stateShares.size() + " shares");
        for (DeRecHelper.Share ss : stateShares) {
            System.out.println("Sharer: " + ss.getSharer().getId().getName() + ", PEK: "
                    + ss.getSharer().getId().getPublicEncryptionKey() + ", Obj: " + ss.getSharer() + "secret: "
                    + Base64.getEncoder().encodeToString(ss.getSecretId().getBytes())
                    + " versions: "
                    + ss.getVersions());
        }
    }

    public void updateSharerFromLib() {
        secrets = new ArrayList<>(sharer.getSecrets());
        System.out.println("In updateSharerFromLib");

        // Update Secrets
        ArrayList<DisplayEntry> updatedSharerTabSecretsList = new ArrayList<>();
        if (sharer != null) {
            System.out.println("In updateSharerFromLib: sharer is not null");
            for (var secret : sharer.getSecrets()) {
                System.out.println("In updateSharerFromLib: secret is "
                        + secret.getSecretId().toString());
                String title = secret.getSecretId().toString();
                String contents = secret.getDescription() + "," + secret.isRecovering() + ", " + secret.isAvailable()
                        + ", " + secret.isClosed();
                System.out.println("Title: " + title + ", contents: " + contents);
                updatedSharerTabSecretsList.add(new DisplayEntry(title, contents, secret));
            }
            if (!updatedSharerTabSecretsList.equals(sharerTabSecretsList)) {
                sharerTabSecretsList = updatedSharerTabSecretsList;
                observableSharerTabSecretsUpdateCounter.set(observableSharerTabSecretsUpdateCounter.get() + 1);
            }
        }
        System.out.println("In updateSharerFromLib: done with secrets");

        if (userSelections.secret != null) {
            // Update HelperStatuses
            ArrayList<DisplayEntry> updatedSharerTabHelpersContents = new ArrayList<>();
            for (var helperStatus : userSelections.secret.getHelperStatuses()) {
                String title = helperStatus.getId().getName();
                String lastVerificationTime = helperStatus.getLastVerificationTime() == null
                        ? "N/A"
                        : formatter.format(
                                helperStatus.getLastVerificationTime().atZone(ZoneId.systemDefault()));
                String contents =
                        "State: " + helperStatus.getStatus().toString() + "\nLast Contact: " + lastVerificationTime;
                updatedSharerTabHelpersContents.add(new DisplayEntry(title, contents, helperStatus));
            }
            if (!updatedSharerTabHelpersContents.equals(sharerTabHelpersContents)) {
                sharerTabHelpersContents = updatedSharerTabHelpersContents;
                observableSharerTabHelpersUpdateCounter.set(observableSharerTabHelpersUpdateCounter.get() + 1);
            }

            // Update versions
            ArrayList<DisplayEntry> updatedSharerTabVersionsContents = new ArrayList<>();
            for (var entry : userSelections.secret.getVersions().entrySet()) {
                String title = "Version: " + entry.getKey();
                String contents = "Protected Data: \n"
                        + new String(entry.getValue().getProtectedValue(), StandardCharsets.UTF_8) + "\n\n"
                        + (entry.getValue().isProtected()
                                ? "This version is protected"
                                : "This version is not " + "protected yet");
                updatedSharerTabVersionsContents.add(new DisplayEntry(title, contents, entry.getValue()));
            }
            if (!updatedSharerTabVersionsContents.equals(sharerTabVersionsContents)) {
                sharerTabVersionsContents = updatedSharerTabVersionsContents;
                observableSharerTabVersionsUpdateCounter.set(observableSharerTabVersionsUpdateCounter.get() + 1);
            }
        }
    }
}
