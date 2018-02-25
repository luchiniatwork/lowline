(ns test
  (:require [lowline.core :as ll]
            [clojure.string :as str]))

;; (def cli (ll/get-cli))

;; ;; Default value

;; (ll/ask cli "Company?" (fn [a] {:value (or a "none")}))

;; ;; Validation

;; (ll/ask cli "Age?" (fn [a] {:valid? (and (integer? a)
;;                                          (> a 0)
;;                                          (< a 105))
;;                             :message "Must be between 0 and 105"}))
;; (ll/ask cli "Name?" (fn [a] {:valid? (re-matches #"^([ \u00c0-\u01ffa-zA-Z'\-])+$" a)}))

;; ;; Type conversion for answers

;; (ll/ask cli "Birthday?" (fn [a] (let [val (clj-time/parse parser a)]
;;                                   {:value val
;;                                    :valid? val})))
;; (ll/ask cli "Interests? (comma sep list)" (fn [a] {:value (str/split #"," a)}))

;; ;; Reading passwords

;; (ll/ask cli "Password:" {:echo false})
;; (ll/ask cli "Password:" {:echo "x"})

;; ;; Menus

;; ;; Single choice by name

;; (ll/menu cli "Choose your favorite language?"
;;          {:options {:ruby {:prompt "The language that makes DHH happy"} ;; could also be nil
;;                     :clojure {:prompt "The language that makes Rich Hickey happy"}
;;                     :na {:fn (fn [a] {:value "N/A"})}}
;;           :option-color [:red :bg-white] ;; some default
;;           :message "Must be ruby, clojure or na"}) ;; message is also default

;; ;; Single choice by index

;; (ll/menu cli "Choose your favorite language?"
;;          {:options {:ruby nil
;;                     :clojure nil
;;                     :na (fn [a] {:value "N/A"})}
;;           :by-index true}) ;; message is similar to above

;; ;; Multiple choice

;; (ll/menu cli "Choose your favorite language(s)?"
;;          {:options {:ruby nil
;;                     :clojure nil
;;                     :na (fn [a] {:value "N/A"})}
;;           :multiple true
;;           :option-color [:red :bg-white] ;; some default
;;           :selected-color [:green :bg-black] ;; some default
;;           :done-option :done ;; it would be default to done anyway
;;           :by-index true ;; not needed (could be by key)
;;           })


;; --------------------------------------------------

(def questions
  [
   ;; Default value (or any other transformation)
   {:id :company
    :prompt "Company?"
    :parser (fn [a] {:value (or a "None")})}

   ;; Validations
   {:id :age
    :prompt "Age?"
    :parser (fn [a]
              {:valid? (and (integer? a)
                            (> a 0)
                            (< a 105))
               :message "Must be between 0 and 105"})}
   {:id :name
    :prompt "Name?"
    :parser (fn [a]
              {:valid? (re-matches #"^([ \u00c0-\u01ffa-zA-Z'\-])+$" a)})}

   ;; Type converstaions
   {:id :interests
    :prompt "Interests? (comma sep list)"
    :parser (fn [a] {:value (str/split #"," a)})}

   ;; Passwords
   {:id :pass1
    :prompt "Password (no echo):"
    :echo false}
   
   {:id :pass2
    :prompt "Password (echo \"x\"):"
    :echo "x"}
   
   ])

(defn -main  []
  (let [answers (ll/process questions)]
    (clojure.pprint/pprint answers)))
