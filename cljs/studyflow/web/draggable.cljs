(ns studyflow.web.draggable
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [goog.fx.Dragger :as fxdrag]))

(defn draggable-item
  [view position-cursor]
  (fn [item owner]
    (reify
      om/IRender
      (render [_]
        (dom/div #js {:style #js {:position "fixed"
                                  :zIndex 10000}}
                 (om/build view item)))
      om/IDidMount
      (did-mount [_]
        (let [el (om/get-node owner)
              dragger (goog.fx.Dragger. el)]
          (events/listen dragger
                         fxdrag/EventType.START
                         (fn [event]
                           (om/update! item :dragging true)))
          (events/listen dragger
                         fxdrag/EventType.END
                         (fn [event]
                           (om/update! item :dragging false))))))))
