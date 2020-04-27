(ns status-im.desktop.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            [taoensso.timbre :as log]
            status-im.subs
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.core :as core]
            [status-im.utils.snoopy :as snoopy]
            [status-im.ui.components.tabbar.core :as tabbar]
            [status-im.ui.screens.desktop.views :as desktop-views]
            [status-im.desktop.deep-links :as deep-links]
            [status-im.utils.config :as config]
            [status-im.ui.components.button :as button]))

(defn app-state-change-handler [state]
  (re-frame/dispatch [:app-state-change state]))

(defn app-root [props]
  (log/info "#desktop app-root")
  [react/view
   [react/text "###Desktop is back!!!!111111"]
   [tabbar/tabs {:index 1 :navigate identity :insets {}}]]
  #_(if config/mobile-ui-for-desktop?
      (reagent/create-class
       {:component-did-mount
        (fn [this]
          (.addEventListener react/app-state "change" app-state-change-handler)
          (re-frame/dispatch [:set-initial-props (reagent/props this)]))
        :component-will-unmount
        (fn []
          (.removeEventListener react/app-state "change" app-state-change-handler))
        :display-name "root"
        :reagent-render views/main})
      (reagent/create-class
       {:component-did-mount (fn [this]
                               (re-frame/dispatch [:set-initial-props (reagent/props this)])
                             ;(shortcuts/register-default-shortcuts)
                               (deep-links/add-event-listener))
        :reagent-render      (fn [props]
                               desktop-views/main)})))

(defn init []
  (core/init app-root))
