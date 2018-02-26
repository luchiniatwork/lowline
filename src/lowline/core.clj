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

(s/def ::echo (s/or :bool boolean?
                    :str #(and (string? %) (= 1 (count %)))))
(expound/defmsg ::echo "should be a either a boolean or a string of exactly one character")

(s/def ::question (s/keys :req-un [::id ::prompt]
                          :opt-un [::parser ::echo]))

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

(defn print-error [msg]
  (println (c/style (str "! " msg) :red)))

(defn print-question [prompt]
  (print (c/style (str "? " prompt " ") :yellow)))

(defn ^:private error-out [questions]
  (expound/expound ::questions questions)
  nil)

(defn ^:private print-validation-message [message]
  (print-error (or message "Invalid input. Try again.")))

(defn ^:private process-parser [parser res]
  (if parser
    (let [{:keys [valid? message value] :as parsed} (parser res)]
      (if (s/valid? ::parsed parsed)
        (if (and (not (nil? valid?))
                 (not valid?))
          (print-validation-message message)
          (or value res))
        (expound/expound ::parsed parsed)))))

(defn ^:private ask [{:keys [id prompt parser echo] :as question}]
  (loop []
    (print-question prompt)
    (flush)
    (let [res (read-line)]
      (if parser
        (if-let [out (process-parser parser res)]
          out
          (recur))
        res))))

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
    (start-questioning (s/conform ::questions questions))
    (error-out questions)))
