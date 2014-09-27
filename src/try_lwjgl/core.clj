(ns try-lwjgl.core
  ;;(:gen-class)
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.loop :as loop]
            [try-lwjgl.display :as display]))

(defn -main
  []
  (println "Try LWJGL")
  (logic/init)
  (display/init)
  (loop/run))
