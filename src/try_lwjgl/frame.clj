(ns try-lwjgl.frame
  (:require [try-lwjgl.models :as models]
            [try-lwjgl.model.player :as model.player]
            [try-lwjgl.input :as input]
            [try-lwjgl.display :as display]
            [try-lwjgl.logic :as logic]))

(defn iteration [delta]
  (models/simulate delta)
  (logic/frame delta)
  (display/draw))
