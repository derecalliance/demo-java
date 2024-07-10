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
