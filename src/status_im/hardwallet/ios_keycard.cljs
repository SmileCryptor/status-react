(ns status-im.hardwallet.ios-keycard
  (:require [status-im.hardwallet.keycard :as keycard]))

(defrecord IOSKeycard []
  keycard/Keycard
  (check-nfc-support [this args])
  (check-nfc-enabled [this args])
  (open-nfc-settings [this])
  (register-card-events [this args])
  (on-card-disconnected [this callback])
  (on-card-connected [this callback])
  (remove-event-listener [this event])
  (remove-event-listeners [this])
  (get-application-info [this args])
  (install-applet [this args])
  (init-card [this args])
  (install-applet-and-init-card [this args])
  (pair [this args])
  (generate-mnemonic [this args])
  (generate-and-load-key [this args])
  (unblock-pin [this args])
  (verify-pin [this args])
  (change-pin [this args])
  (unpair [this args])
  (delete [this args])
  (remove-key [this args])
  (remove-key-with-unpair [this args])
  (export-key [this args])
  (unpair-and-delete [this args])
  (get-keys [this args])
  (sign [this args]))
