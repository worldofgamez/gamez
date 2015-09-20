(ns gamez.forms.core
  (:require
    [schema.core :as s]
    [gamez.util.shared :as gus]))

(defn signup-forms
  "Renders the forms for signup"
  []
  {:schema
   {:email  s/Str
    :name s/Str
    :birthday s/Inst
    :password s/Str}

   :validations
   [[:before (gus/ten-year-old) :birthday]
    [:min-length 8 :password]
    [:email :email]]
   }
  )

