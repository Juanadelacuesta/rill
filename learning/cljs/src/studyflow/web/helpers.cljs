(ns studyflow.web.helpers
  (:require [om.dom :as dom]
            [goog.string :as gstring]
            [clojure.string :as string]))

(defn raw-html
  [raw]
  (dom/span #js {:dangerouslySetInnerHTML #js {:__html raw}} nil))

(defn update-js [js-obj key f]
  (let [key (if (keyword? key)
              (name key)
              key)
        p (.-props js-obj)]
    (aset p key (f (get p key)))
    js-obj))

(defn modal [content primary-button & [secondary-button]]
  (dom/div #js {:id "m-modal"
                :className "show"}
           (dom/div #js {:className "modal_inner"}
                    content
                    (dom/div #js {:className "modal_footer"}
                             (when secondary-button
                               (update-js secondary-button
                                          :className (fnil (partial str "btn big gray") "")))
                             (update-js primary-button
                                        :className (partial str "btn big yellow pull-right"))))))

(defn split-text-and-inputs [text inputs]
  (reduce
   (fn [pieces input]
     (loop [[p & ps] pieces
            out []]
       (if-not p
         out
         (if (gstring/contains p input)
           (let [[before & after] (string/split p (re-pattern input))]
             (-> out
                 (into [before input])
                 (into after)
                 (into ps)))
           (recur ps (conj out p))))))
   [text]
   inputs))


(defn render-question [question-text inputs]
  (apply dom/div nil
         (for [text-or-input (split-text-and-inputs question-text
                                                    (keys inputs))]
           ;; this wrapper div is
           ;; required, otherwise the
           ;; dangerouslySetInnerHTML
           ;; breaks when mixing html
           ;; in text and inputs
           (dom/div #js {:className "dangerous-html-wrap"}
                    (if-let [input (get inputs text-or-input)]
                      input
                      (raw-html text-or-input))))))