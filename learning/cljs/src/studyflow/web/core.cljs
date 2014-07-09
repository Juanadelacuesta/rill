(ns studyflow.web.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [studyflow.web.service :as service]
            [clojure.string :as string]))


(enable-console-print!)

(defn course-id-for-page []
  (let [loc (.. js/document -location -pathname)]
    (last (string/split loc "/"))))

(def app-state (atom {:course-id  (course-id-for-page)}))


(defn navigation [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (println "Navigation will mount"))
    om/IRender
    (render [_]
      (if-let [course (get cursor :course-material)]
        (dom/div nil
                 (dom/h1 #js {:data-id (:id course)} (:name course))
                 (apply dom/ul nil
                        (for [{:keys [title sections]
                               chapter-id :id
                               :as chapter} (:chapters course)]
                          (dom/li #js {:data-id chapter-id}
                                  title
                                  (apply dom/ul nil
                                         (for [{:keys [title]
                                                section-id :id
                                                :as section} sections]
                                           (dom/a #js {:href (str "#section-" section-id)
                                                       :onClick
                                                       (fn [_]
                                                         (om/update! cursor
                                                                     :selected-section
                                                                     [chapter-id section-id]))}
                                                  (dom/li #js {:data-id section-id}
                                                          title))))))))
        (dom/h2 nil "No content ... spinner goes here")))
    om/IWillUnmount
    (will-unmount [_]
      (println "Navigation will unmount"))))

(defn question-by-id [cursor section-id id]
  (first (filter #(= id (:id %))
                 (-> cursor :sections-data (get section-id) :questions))))

(defn section-test [cursor owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "section-test"}
               (let [[_ section-id] (get-in cursor [:selected-section])]
                 (if (:section-id-for-section-test cursor)
                   (if-let [question-id (:current-section-test-question-id cursor)]
                     (let [question (question-by-id cursor section-id question-id)
                           text (:text question)
                           first-part (.replace text (re-pattern (str "__INPUT_1__.*$")) "")
                           last-part (.replace text (re-pattern (str "^.*__INPUT_1__")) "")]
                       (dom/div nil
                                (dom/div #js {:dangerouslySetInnerHTML #js {:__html first-part}} nil)
                                (dom/input #js {:value (get cursor :current-answer "")
                                                :onChange (fn [event]
                                                            (om/transact! cursor #(assoc % :current-answer (.. event -target -value))))})
                                (dom/div #js {:dangerouslySetInnerHTML #js {:__html last-part}} nil)
                                (when-let [current-answer (:current-answer cursor)]
                                  (let [current-section-test-id (:current-section-test-id cursor)
                                        course-id (:course-id cursor)]
                                    (dom/button #js {:onClick (fn []
                                                                (om/transact! cursor
                                                                              [:command-queue]
                                                                              #(conj %
                                                                                     ["section-test-commands/check-answer"
                                                                                      current-section-test-id
                                                                                      section-id
                                                                                      course-id
                                                                                      question-id
                                                                                      {"__INPUT_1__" current-answer}])))}
                                                "!")))))
                     "...")
                   (dom/button #js {:onClick
                                    (fn [_]
                                      (om/update! cursor
                                                  :section-id-for-section-test
                                                  section-id))}
                               "GO!")))))))

(defn content [cursor owner]
  (reify
    om/IRender
    (render [_]
      (if-let [[_ section-id] (get-in cursor [:selected-section])]
        (if-let [section-data (get-in cursor [:sections-data section-id])]
          (dom/div #js {:id (str "section-" section-id)}
                   (dom/h2 nil (:title section-data))
                   (om/build section-test cursor))
          (dom/h2 nil (str "Selected data not yet loaded" section-id)))
        (dom/h2 nil "No section selected")))))

(defn widgets [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (println "widget will mount"))
    om/IRender
    (render [_]
      (dom/div nil
               (om/build navigation cursor)
               (om/build content cursor)))
    om/IWillUnmount
    (will-unmount [_]
      (println "widget will unmount"))))

(defn ^:export course-page []
  (om/root
   (service/wrap-service widgets)
   app-state
   {:target (. js/document (getElementById "app"))
    :tx-listen service/listen}))