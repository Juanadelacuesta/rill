(ns studyflow.web.root
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [studyflow.web.core :as core]
            [studyflow.web.entry-quiz :as entry-quiz]
            [studyflow.web.chapter-quiz :as chapter-quiz]
            [om.dom :as dom]
            [om.core :as om]
            [goog.dom :as gdom]
            [studyflow.web.dashboard :refer [dashboard]]
            [studyflow.web.history :as url-history]
            [studyflow.web.helpers :refer [modal element-top] :as helpers]
            [studyflow.web.ipad :as ipad]
            [studyflow.web.tracking :as tracking]
            [studyflow.web.service :as service]
            [studyflow.web.section :as section]
            [studyflow.web.history :refer [navigate-to-path]]
            [cljs.core.async :as async :refer [<!]]))

(defn running-chapter-quiz
  [cursor]
  (let [material (get-in cursor [:view :course-material])]
    (first (filter (fn [c]
                     (let [aggregate (get-in cursor [:aggregates (:id c)])]
                       (and (#{"running" "running-fast-track"} (-> c :chapter-quiz :status))
                            (if aggregate
                              (= :running (:status aggregate))
                              true))))
                   (:chapters material)))))

(defn widgets [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (core/watch-notifications! (om/get-shared owner :notification-channel) cursor))
    om/IRender
    (render [_]
      (let [{:keys [main section-tab]} (get-in cursor [:view :selected-path])]
        (when-let [quiz (running-chapter-quiz cursor)]
          (navigate-to-path {:main :chapter-quiz
                             :chapter-id (:id quiz)}))
        (dom/div #js {:className (if (and (= main :learning)
                                          (= section-tab :questions))
                                   "questions_page"
                                   "")}
                 (when (get-in cursor [:aggregates :failed])
                   (modal (dom/h1 nil "Je bent niet meer up-to-date met de server. Herlaad de pagina.")
                          "Herlaad de pagina"
                          (fn [e]
                            (.reload js/location true))))

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
                            (om/build section/page-header cursor)
                            (om/build section/navigation-panel cursor)
                            (om/build section/section-panel cursor))
                   ;; default
                   (om/build dashboard cursor)))))))

(defn start-scrolling-listener [last-scroll-time]
  (let [scrolling-channel  (async/chan (async/sliding-buffer 1))]
    (set! (.-onscroll js/document)
          (fn [e]
            (swap! last-scroll-time (fn [_] (.getTime (js/Date.))))
            (async/put! scrolling-channel {:pos (.-pageYOffset js/window)})))
    (go-loop []
      (let [{:keys [pos]} (<! scrolling-channel)
            section (.getElementById js/document "m-section")]
        (when section
          (let [offsets (map-indexed (fn [i el]
                                       [i (element-top el)])
                                     (filter #(= (.-className %) "m-subsection")
                                             (array-seq (gdom/getChildren section) 0)))
                subsection-index (if (>= (+ pos
                                            200
                                            (.-innerHeight js/window))
                                         (.-scrollHeight js/document.body))
                                   (first (last offsets))
                                   (first (last (filter (fn [[i offset]]
                                                          (< offset (+ 300 pos)))
                                                        offsets))))]
            (when subsection-index
              (let [current-location (.-href (.-location js/document))
                    [before hash] (.split current-location "#")
                    new-location (apply str before "#"
                                        (interpose "/" (concat (take 4 (.split hash "/"))
                                                               [subsection-index])))]
                (when-not (= new-location current-location)
                  (.replace (.-location js/document) new-location))))))
        (recur)))))

(defn load-course-material [widgets]
  (fn [cursor owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (async/put! (om/get-shared owner :data-channel)
                    ["data/course-material" (get-in cursor [:static :course-id]) (get-in cursor  [:static :student :id])]))
      om/IRender
      (render [_]
        (if (get-in cursor [:view :course-material])
          (om/build widgets cursor)
          (dom/div #js {:id "dashboard_page"}
                   (dom/header #js {:id "m-top_header"}
                               (dom/h1 #js {:id "logo"} "Laden ..."))))))))

(defn ^:export course-page []
  (let [command-channel (async/chan)
        last-scroll (atom 0)]
    (om/root
     (service/wrap-service
      (load-course-material
       (url-history/wrap-history
        widgets)))
     (core/init-app-state)
     {:target (gdom/getElement "app")
      :tx-listen (fn [tx-report cursor]
                   (service/listen tx-report cursor)
                   (url-history/listen tx-report cursor)
                   (tracking/listen tx-report cursor command-channel))
      :shared {:command-channel command-channel
               :data-channel (async/chan)
               :notification-channel (async/chan)
               :last-scroll last-scroll}})
    (start-scrolling-listener last-scroll)))

(ipad/ipad-scroll-on-inputs-blur-fix)
