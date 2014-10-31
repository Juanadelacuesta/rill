(ns studyflow.web.root
  (:require [studyflow.web.core :as core]
            [studyflow.web.entry-quiz :as entry-quiz]
            [studyflow.web.chapter-quiz :as chapter-quiz]
            [om.dom :as dom]
            [om.core :as om]
            [goog.dom :as gdom]
            [studyflow.web.history :as url-history]
            [studyflow.web.helpers :refer [modal] :as helpers]
            [studyflow.web.tracking :as tracking]
            [studyflow.web.service :as service]
            [cljs.core.async :as async]))

(defn widgets [cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [main section-tab]} (get-in cursor [:view :selected-path])]
        (dom/div #js {:className (if (and (= main :learning)
                                          (= section-tab :questions))
                                   "questions_page"
                                   "")}
                 (when (get-in cursor [:aggregates :failed])
                   (modal
                    (dom/h1 nil "Je bent niet meer up-to-date met de server. Herlaad de pagina.")
                    (dom/button #js {:onClick (fn [e]
                                                (.reload js/location true))}
                                "Herlaad de pagina")))
                 (when-not (= :entry-quiz
                              (get-in cursor [:view :selected-path :main]))
                   (entry-quiz/entry-quiz-modal cursor owner))
                 (when (get-in cursor [:view :chapter-quiz-modal :show])
                   (om/build chapter-quiz/chapter-quiz-modal cursor))
                 (case (get-in cursor [:view :selected-path :main])
                   :entry-quiz
                   (om/build entry-quiz/entry-quiz-panel cursor)
                   :chapter-quiz
                   (om/build chapter-quiz/chapter-quiz-panel cursor)
                   :learning
                   (dom/div nil
                            (om/build core/page-header cursor)
                            (om/build core/navigation-panel cursor)
                            (om/build core/section-panel cursor))
                   ;; default
                   (om/build core/dashboard cursor)))))))

(defn ^:export course-page []
  (let [command-channel (async/chan)]
      (om/root
       (-> widgets
           service/wrap-service
           url-history/wrap-history)
       (core/init-app-state)
       {:target (gdom/getElement "app")
        :tx-listen (fn [tx-report cursor]
                     (service/listen tx-report cursor)
                     (url-history/listen tx-report cursor)
                     (tracking/listen tx-report cursor command-channel))
        :shared {:command-channel command-channel
                 :data-channel (async/chan)
                 :notification-channel (async/chan)}})))

(helpers/ipad-scroll-on-inputs-blur-fix)
