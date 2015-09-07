(ns gamez.model.message)

(def schema
  [{:$name :message
    :id         :uuid.pk
    :subject    :text.fullindex
    :body       :text.fullindex
    :thread     :uuid.index
    :sender :uuid.index
    :deleted :bool
    :data :json
    :created_at :date.now}
   {:name :message_recipients
    :id :uuid.pk
    :who :uuid.index$person.id
    :type :text
    :data :json}
   {:name :message_history
    :id :uuid.pk
    :msg_id :uuid.index$message.id
    :created_at :date.now
    :data :json}
   ])