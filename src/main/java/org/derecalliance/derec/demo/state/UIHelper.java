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

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.derecalliance.derec.lib.api.DeRecHelper;
// import org.derecalliance.derec.api.DeRecHelper;

public class UIHelper {
    ObservableList<DeRecHelper.SharerStatus> sharerStatuses;
    ObservableList<DeRecHelper.Share> shares = null;

    UIHelper() {
        sharerStatuses = FXCollections.observableArrayList();
        shares = FXCollections.observableArrayList();
    }

    public void updateFromLib() {
        List<? extends DeRecHelper.SharerStatus> sharerStatusesFromLib =
                State.getInstance().getHelper().getSharers();
        sharerStatuses.addAll(sharerStatusesFromLib);

        List<? extends DeRecHelper.Share> sharesFromLib =
                State.getInstance().getHelper().getShares();
        shares.addAll(sharesFromLib);
    }
}
