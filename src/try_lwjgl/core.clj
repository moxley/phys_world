(ns try-lwjgl.core
  ;;(:gen-class)
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.loop :as loop]
            [try-lwjgl.display :as display]
            [try-lwjgl.models :as models]))

(defn -main
  []
  (println "Try LWJGL")
  (models/init)
  (logic/init)
  (display/init)
  (loop/run))
