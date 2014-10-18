(ns try-lwjgl.frame
  (:require [try-lwjgl.models :as models]
            [try-lwjgl.model.player :as model.player]
            [try-lwjgl.input :as input]
            [try-lwjgl.display :as display]))

(defn handle-input [delta]
  (model.player/logic delta @models/player)
  (doseq [event (input/collect-key-events)]
    (let [[key down? repeat?] (map #(event %) [:key :down? :repeat?])]
      (cond
       (= :g key) (input/set-mouse-grabbed true)
       (= :r key) (input/set-mouse-grabbed false)))))

(defn iteration [delta]
  (models/simulate delta)
  (handle-input delta)
  (display/draw))
