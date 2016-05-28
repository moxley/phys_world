(ns phys-world.frame
  (:require [phys-world.models :as models]
            [phys-world.model.player :as model.player]
            [phys-world.input :as input]
            [phys-world.display :as display]
            [phys-world.logic :as logic]))

(defn iteration [delta]
  (models/simulate delta)
  (logic/frame delta)
  (display/draw))
