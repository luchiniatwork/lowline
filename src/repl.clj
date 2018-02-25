(ns repl
  (:require cider-nrepl.main))

(defn -main []
  (cider-nrepl.main/init ["cider.nrepl/cider-middleware"]))
