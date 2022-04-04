(ns status-im.stickers.core
  (:require [clojure.set :as clojure.set]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.navigation :as navigation]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]))

(re-frame/reg-fx
 :stickers/set-pending-timeout-fx
 (fn []
   (utils/set-timeout #(re-frame/dispatch [:stickers/pending-timeout])
                      10000)))

(fx/defn install-stickers-pack
  {:events [:stickers/install-pack]}
  [{db :db :as cofx} id]
  (let [pack (get-in db [:stickers/packs id])]
    (fx/merge
     cofx
     {:db             (assoc-in db [:stickers/packs id] (assoc pack :status 1))
      ::json-rpc/call [{:method     "stickers_install"
                        :params     [(ethereum/chain-id db) id]
                        :on-success #()}]})))

(fx/defn load-packs
  {:events [:stickers/load-packs]}
  [{:keys [db]}]
  {::json-rpc/call [{:method     "stickers_market"
                     :params     [(ethereum/chain-id db)]
                     :on-success #(re-frame/dispatch [:stickers/stickers-market-success %])}
                    {:method     "stickers_installed"
                     :params     []
                     :on-success #(re-frame/dispatch [:stickers/stickers-installed-success %])}
                    {:method     "stickers_pending"
                     :params     []
                     :on-success #(re-frame/dispatch [:stickers/stickers-pending-success %])}
                    {:method     "stickers_recent"
                     :params     []
                     :on-success #(re-frame/dispatch [:stickers/stickers-recent-success %])}]})

(fx/defn buy-pack
  {:events [:stickers/buy-pack]}
  [{db :db} pack-id]
  {::json-rpc/call [{:method     "stickers_buyPrepareTx"
                     :params     [(ethereum/chain-id db) (ethereum/default-address db) (int pack-id)]
                     :on-success #(re-frame/dispatch [:signing.ui/sign
                                                      {:tx-obj    %
                                                       :on-result [:stickers/pending-pack pack-id]}])}]})

(fx/defn pending-pack
  {:events [:stickers/pending-pack]}
  [{db :db :as cofx} id]
  (fx/merge cofx
            {:db                              (update db :stickers/packs-pending conj id)
             :stickers/set-pending-timeout-fx nil
             ::json-rpc/call                  [{:method     "stickers_AddPending"
                                                :params     [(ethereum/chain-id db) (int id)]
                                                :on-success #()}]}))

(fx/defn pending-timeout
  {:events [:stickers/pending-timeout]}
  [{{:stickers/keys [packs-pending] :as db} :db}]
  (when (seq packs-pending)
    {::json-rpc/call [{:method     "stickers_processPending"
                       :params     [(ethereum/chain-id db)]
                       :on-success #(re-frame/dispatch [:stickers/stickers-process-pending-success %])}]}))

(fx/defn stickers-process-pending-success
  {:events [:stickers/stickers-process-pending-success]}
  [{{:stickers/keys [packs-pending] :as db} :db} purchased]
  (let [packs-pending (apply disj packs-pending purchased)]
    (merge
     {:db (assoc db :stickers/packs-pending packs-pending)}
     ;;todo update  purchased in packs
     (when (seq packs-pending)
       {:stickers/set-pending-timeout-fx nil}))))

(fx/defn stickers-market-success
  {:events [:stickers/stickers-market-success]}
  [{:keys [db]} packs]
  (let [packs (reduce (fn [acc pack] (assoc acc (:id pack) pack)) {} packs)]
    {:db (update db :stickers/packs merge packs)}))

(fx/defn stickers-installed-success
  {:events [:stickers/stickers-installed-success]}
  [{:keys [db]} packs]
  (let [packs (reduce (fn [acc [_ pack]] (assoc acc (:id pack) pack)) {} packs)]
    {:db (update db :stickers/packs merge packs)}))

(fx/defn stickers-pending-success
  {:events [:stickers/stickers-pending-success]}
  [{:keys [db]} packs]
  (let [packs (reduce (fn [acc [_ pack]] (conj acc (:id pack))) #{} packs)]
    (merge
     {:db (assoc db :stickers/packs-pending packs)}
     (when (seq packs)
       {:stickers/set-pending-timeout-fx nil}))))

(fx/defn stickers-recent-success
  {:events [:stickers/stickers-recent-success]}
  [{:keys [db]} packs]
  {:db (assoc db :stickers/recent-stickers packs)})

(fx/defn open-sticker-pack
  {:events [:stickers/open-sticker-pack]}
  [{{:networks/keys [current-network]} :db :as cofx} id]
  (when (and id (string/starts-with? current-network "mainnet"))
    (navigation/open-modal cofx :stickers-pack {:id id})))

(fx/defn select-pack
  {:events [:stickers/select-pack]}
  [{:keys [db]} id]
  {:db (assoc db :stickers/selected-pack id)})
