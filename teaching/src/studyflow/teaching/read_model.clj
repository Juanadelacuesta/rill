(ns studyflow.teaching.read-model
  (:require [clojure.string :as str]))

(def empty-model {})

(defn classes [model]
  (map #(let [name (:class-name %)
              department-id (:department-id %)
              department (get-in model [:departments department-id])
              school-id (:school-id department)
              school (get-in model [:schools school-id])]
          {:id (str school-id "|" department-id "|" name)
           :name name
           :full-name (str/join " - " [(:name school) (:name department) name])
           :department-id department-id
           :department-name (:name department)
           :school-id school-id
           :school-name (:name school)})
       (set (map #(select-keys % [:department-id :class-name])
                 (filter :class-name
                         (vals (:students model)))))))

(defn students-for-class [model class]
  (filter (fn [student]
            (and (= (:department-id student) (:department-id class))
                 (= (:class-name student) (:name class))))
          (vals (:students model))))

;; catchup

(defn caught-up
  [model]
  (assoc model :caught-up true))

(defn caught-up?
  [model]
  (boolean (:caught-up model)))
