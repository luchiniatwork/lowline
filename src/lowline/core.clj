(ns lowline.core
  (:require [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [clansi :as c]))

;; --------------------------------------------------

(defn ^:private arg-count [f]
  (->> f
       class
       .getDeclaredMethods
       (filter #(= "invoke" (.getName %)))
       first
       .getParameterTypes
       alength))

(s/def ::id keyword?)

(s/def ::prompt string?)

(s/def ::parser (s/and ifn? #(= 1 (arg-count %))))
(expound/defmsg ::parser "should be a function with exactly one argument")



(s/def ::base-question (s/keys :req-un [::id ::prompt]
                               :opt-un [::parser]))

(s/def ::options (s/coll-of ::base-question
                            :kind vector?
                            :min-count 1))

(s/def ::menu-question (s/keys :req-un [::id ::prompt ::options]))

(s/def ::question (s/or :base-question ::base-question
                        :menu-question ::menu-question))

(s/def ::questions (s/coll-of ::question
                              :kind vector?
                              :min-count 1))
(expound/defmsg ::questions "should be a vector of questions")

;; --------------------------------------------------

(s/def ::valid? boolean?)

(s/def ::message string?)

(s/def ::value #(instance? java.lang.Object %))

(s/def ::parsed-validator (s/keys :req-un [::valid?]
                                  :opt-un [::message ::value]))

(s/def ::parsed-converter (s/keys :req-un [::value]
                                  :opt-un [::valid? ::message]))

(s/def ::parsed (s/or :validator ::parsed-validator
                      :converter ::parsed-converter))


;; --------------------------------------------------

(defn ^:private print-error [msg] 
  (println (c/style (str "! " msg) :red)))

(defn ^:private print-question [prompt]
  (print (c/style (str "? " prompt " ") :yellow)))

(defn ^:private print-options [options]
  (if options
    (doseq [{:keys [id prompt]} options]
      (println id prompt))))

(defn ^:private error-out [questions]
  (expound/expound ::questions questions)
  nil)

(defn ^:private print-validation-message [message]
  (print-error (or message "Invalid input. Try again.")))

;; --------------------------------------------------

(defn ^:private process-parser [parser res]
  (if parser
    (let [{:keys [valid? message value] :as parsed} (parser res)]
      (if (s/valid? ::parsed parsed)
        (if (and (not (nil? valid?))
                 (not valid?))
          (print-validation-message message)
          (or value res))
        (expound/expound ::parsed parsed)))))

(defn ^:private ask [{:keys [id prompt options parser] :as question}]
  (loop []
    (print-question prompt)
    (print-options options)
    (flush)
    (let [res (read-line)]
      (if options
        (if (some #(= res (name %)) (map #(:id %) options))
          (keyword res)
          (do
            (print-validation-message (:message question))
            (recur)))
        (if parser
          (if-let [out (process-parser parser res)]
            out
            (recur))
          res)))))

(defn ^:private start-questioning [questions]
  (let [out (atom {})]
    (doseq [question questions]
      (let [res (ask question)]
        (swap! out assoc (:id question) res)
        (println "<" res)))
    @out))

;; --------------------------------------------------

(defn process [questions]
  (if (s/valid? ::questions questions)
    (start-questioning questions)
    (error-out questions)))
