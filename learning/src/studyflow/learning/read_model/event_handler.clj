(ns studyflow.learning.read-model.event-handler
  (:require [clojure.tools.logging :as log]
            [studyflow.learning.read-model :as m]
            [studyflow.learning.course.events :as events]
            [studyflow.learning.section-test.events :as section-test]
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

(defmethod handle-event ::events/Published
  [model event]
  (m/set-course model (:course-id event) (:material event)))

(defmethod handle-event ::events/Updated
  [model event]
  (m/set-course model (:course-id event) (:material event)))

(defmethod handle-event ::events/Deleted
  [model event]
  (m/remove-course model (:course-id event)))

(defmethod handle-event ::section-test/Finished
  [model {:keys [student-id section-id]}]
  (m/set-student-section-status model section-id student-id :finished))

(defmethod handle-event ::section-test/QuestionAssigned
  [model {:keys [student-id section-id]}]
  (m/set-student-section-status model section-id student-id :in-progress))

(defmethod handle-event :studyflow.school-administration.student.events/Created
  [model event]
  (log/debug "Adding student to read-model: " event)
  (m/set-student model (:student-id event) {:full-name (:full-name event)}))

(defmethod handle-event :default
  [model event]
  (log/debug "learning read-model does not handle event" (message/type event))
  model)
