(ns studyflow.web.history
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as gevents]
            [clojure.string :as string])
  (:import [goog.history EventType]
           [goog History]))

(def history (History.))

(defn token->path [token]
  (let [[main-token chapter-id section-id question-token] (string/split token #"/")]
    {:main (keyword main-token)
     :chapter-id (when (seq chapter-id)
                   chapter-id)
     :section-id (when (seq section-id)
                   section-id)
     :section-tab (if (= question-token "questions")
                    :questions
                    :explanation)}))

(defn path->token [path]
  (let [{:keys [main chapter-id section-id section-tab]} path]
    (string/join "/" [(if main
                        (name main)
                        "dashboard")
                      chapter-id section-id
                      (if (= section-tab :questions)
                        "questions"
                        "text")])))

(defn wrap-history [widgets]
  (fn [cursor owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (gevents/listen history EventType.NAVIGATE
                        (fn [event]
                          (when-let [path (token->path (.-token event))]
                            (om/update! cursor
                                        [:view :selected-path]
                                        path)))))
      om/IRender
      (render [_]
        (om/build widgets cursor))
      om/IDidMount
      (did-mount [_]
        ;; setEnabled fires NAVIGATE event for first load
        (.setEnabled history true)
        ))))

(defn listen [tx-report cursor]
  (let [{:keys [path old-state new-state]} tx-report]
    (when (= path [:view :selected-path])
      (when-let [token (path->token (get-in new-state [:view :selected-path]))]
        (.setToken history token)))))

(defn history-link [selected-path]
  (str "#" (path->token selected-path)))