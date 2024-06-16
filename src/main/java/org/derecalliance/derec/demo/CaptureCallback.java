package org.derecalliance.derec.demo;

import org.derecalliance.derec.lib.api.DeRecContact;

@FunctionalInterface
public interface CaptureCallback {
    void onCaptureComplete(DeRecContact scannedContact);
}