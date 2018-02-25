(ns lowline.core
  (:require [clojure.spec.alpha :as s]
            [lanterna.terminal :as t]
            [expound.alpha :as expound]
            [clansi :as c])
  (:import java.util.Scanner))

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

(defn bla []
  (let [term (t/get-terminal :text)]
    (t/start term)
    (t/put-character term \H)
    (t/put-character term \e)
    (Thread/sleep 5000)
    (t/stop term)))

(defn foo []
  (c/style "Test" :underline))

(defn bar []
  (let [s (Scanner. System/in)]
    (.useDelimiter s "")
    (println (.next s))
    (.close s)))

(defn -main []
  (println "Hello from lowline.core")
  (bla))


(defn ^:private error-out [questions]
  (expound/expound ::questions questions)
  nil)

(defn ^:private ask [term {:keys [id prompt parser echo] :as question}]
  (t/put-string term prompt)
  (let [out (atom "")]
    (loop []
      (let [k (t/get-key-blocking term)]
        (if (= :enter k)
          @out
          (do
            (t/put-character term k)
            (swap! out str k)
            (recur)))))))

(defn ^:private start-questioning [questions]
  (let [term (t/get-terminal :text)
        out (atom {})]
    (t/start term)
    (doseq [question questions]
      (swap! out assoc (:id question) (ask term question)))
    (t/stop term)
    @out))

(defn process [questions]
  (if (s/valid? ::questions questions)
    (start-questioning (s/conform ::questions questions))
    (error-out questions)))
