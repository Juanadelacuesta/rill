(ns rill.handler
  (:require [clojure.tools.logging :as log]
            [rill.aggregate :as aggregate]
            [rill.event-store :as store]
            [rill.event-stream :as stream]
            [rill.message :as message :refer [->type-keyword]]
            [clojure.tools.logging :as log]))

(defn valid-commit?
  [[event & events]]
  ;; Every event must apply to the same aggregate root
  (and event
       (let [id (message/primary-aggregate-id event)]
         (every? #(= id (message/primary-aggregate-id %)) events))))

(defn validate-commit
  [events]
  (when-not (valid-commit? events)
    (throw (Exception. "Transactions must apply to exactly one aggregate"))))

(defn commit-events
  [store stream-id from-version events]
  (validate-commit events)
  (log/info ["committing events" events])
  (let [stream-id-from-event (message/primary-aggregate-id (first events))]
    (if (= stream-id stream-id-from-event)
                                        ; events apply to current aggregate
      (store/append-events store stream-id from-version events)
                                        ; events apply to newly created aggregate
      (store/append-events store stream-id-from-event stream/empty-stream-version events))))

(defn load-aggregate-and-version
  [events]
  (reduce (fn [[aggregate version] event]
            [(aggregate/handle-event aggregate event) (message/number event)])
          [nil stream/empty-stream-version]
          events))

(defn prepare-aggregates
  "fetch the primary event stream id and version and aggregates for `command`"
  [event-store command]
  (let [id (message/primary-aggregate-id command)
        additional-ids (aggregate/aggregate-ids command)
        [aggregate current-version] (load-aggregate-and-version (store/retrieve-events event-store id))
        additional-aggregates (map #(aggregate/load-aggregate (store/retrieve-events event-store %)) additional-ids)]
    (into [id current-version aggregate] additional-aggregates)))

(defn try-command
  [event-store command]
  (let [[id version & [primary-aggregate & rest-aggregates]] (prepare-aggregates event-store command)]
    (log/debug [:try-command command])
    (let [result (if-let [events (apply aggregate/handle-command primary-aggregate command rest-aggregates)]
                   (if (commit-events event-store id version events)
                     [:ok events]
                     [:conflict])
                   [:rejected])]
      (log/debug [result])
      result)))