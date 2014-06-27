(ns studyflow.learning.read-model.event-handler
  (:require [studyflow.learning.read-model :as m]
            [studyflow.events :as events]
            [rill.message :as message]))

(defmulti handle-event
  "Update the read model with the given event"
  (fn [model event] (message/type event)))

(defn update-model
  [model events]
  (reduce handle-event model events))

(defn init-model
  [initial-events]
  (update-model nil initial-events)) 

(defmethod handle-event :course-published
  [model event]
  (m/set-course model (:course-id event) (:material event)))

(defmethod handle-event :course-updated
  [model event]
  (m/set-course model (:course-id event) (:material event)))

(defmethod handle-event :course-deleted
  [model event]
  (m/remove-course model (:course-id event)))





