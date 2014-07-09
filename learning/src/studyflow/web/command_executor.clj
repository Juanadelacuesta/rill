(ns studyflow.web.command-executor
  (:require [clojure.tools.logging :as log]
            [rill.handler :as es-dispatcher]
            [rill.message :as message]))

(defn wrap-command-executor
  "Given a set of ring handler that returns a command (or nil), execute
  the command with the given event store and return status 500 or 200"
  [ring-handler event-store]
  (fn [request]
    (when-let [command (ring-handler request)]
      (log/info ["Executing command" command])
      (let [[status events] (es-dispatcher/try-command event-store command)]
        (case status
            :rejected {:status 422 :body {:status :command-rejected}} ; HTTP 422 Unprocessable Entity
            :conflict {:status 409 :body {:status :command-conflict}} ; HTTP 409 Conflict
            :ok {:status 200 :body {:status :command-accepted, :events events}}
            {:status 500 :body {:status :internal-error}})))))