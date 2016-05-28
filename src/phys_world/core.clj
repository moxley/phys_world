(ns phys-world.core
  ;;(:gen-class)
  (:require [phys-world.logic :as logic]
            [phys-world.loop :as loop]
            [phys-world.display :as display]
            [phys-world.models :as models]))

(defn -main
  []
  (println "Try LWJGL")
  (models/init)
  (logic/init)
  (display/init)
  (loop/run))
