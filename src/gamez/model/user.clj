(ns gamez.model.user
  (require
    [gamez.model.db :as db]))

(def schema
  [{:$name :person
    :id         :uuid.pk
    :alias :text.index
    :data :json
    :created_at :date.now}

   {:name :person_history
    :id :uuid.pk
    :person_id :uuid.index$person.id
    :created_at :date.now
    :data :json}
   ])

